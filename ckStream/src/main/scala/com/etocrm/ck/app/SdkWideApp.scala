package com.etocrm.ck.app

import java.lang
import java.util.Properties

import com.alibaba.fastjson.JSON
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import java.math.BigDecimal

import com.alibaba.fastjson.serializer.SerializeConfig
import com.etocrm.ck.Salary.Salary
import com.etocrm.ck.util.{MyKafkaUtil, OffsetManager, PropertiesUtil}
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable.ListBuffer

/**
 * @Author qi.li
 * @create 2020/12/7 9:55
 */
object SdkWideApp {

  def main(args: Array[String]): Unit = {

    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("sdk_wide_app")
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    val sdkInfoGroupId = "WeChatLogConsumerGroup"
    val sdkInfoTopic = "liqi1"

    //1   从redis中读取偏移量 （启动执行一次）
    val sdkInfoOffsetMapForKafka: Map[TopicPartition, Long] = OffsetManager.getOffset(sdkInfoTopic, sdkInfoGroupId)

    //2   把偏移量传递给kafka ，加载数据流（启动执行一次）
    var sdkInfoRecordInputDstream: InputDStream[ConsumerRecord[String, String]] = null
    if (sdkInfoOffsetMapForKafka != null && sdkInfoOffsetMapForKafka.size > 0) { //根据是否能取到偏移量来决定如何加载kafka 流
      sdkInfoRecordInputDstream = MyKafkaUtil.getKafkaStream(sdkInfoTopic, ssc, sdkInfoOffsetMapForKafka, sdkInfoGroupId)
    } else {
      sdkInfoRecordInputDstream = MyKafkaUtil.getKafkaStream(sdkInfoTopic, ssc, sdkInfoGroupId)
    }

    //3   从流中获得本批次的 偏移量结束点（每批次执行一次）
    var sdkInfoOffsetRanges: Array[OffsetRange] = null //周期性储存了当前批次偏移量的变化状态，重要的是偏移量结束点
    val orderInfoInputGetOffsetDstream: DStream[ConsumerRecord[String, String]] = sdkInfoRecordInputDstream.transform { rdd => //周期性在driver中执行
      sdkInfoOffsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd
    }

    // 1 提取数据
    val sdkInfoDstream: DStream[Salary] = orderInfoInputGetOffsetDstream.map { record =>
      val jsonString: String = record.value()
      val orderInfo: Salary = JSON.parseObject(jsonString, classOf[Salary])
      orderInfo
    }

    sdkInfoDstream.persist(StorageLevel.MEMORY_ONLY)
    sdkInfoDstream.print(1000)

    //jdbc sql

    val sparkSession: SparkSession = SparkSession.builder().appName("sdk_wide_app").getOrCreate()


    val config = PropertiesUtil.load("config.properties")
    val user = config.getProperty("clickhouse.user")
    val password = config.getProperty("clickhouse.password")
    val database = config.getProperty("clickhouse.database")
    val connectionProperty = new Properties()
    connectionProperty.setProperty("user", user)
    connectionProperty.setProperty("password", password)
    connectionProperty.setProperty("databases", database)

    import sparkSession.implicits._
    sdkInfoDstream.foreachRDD { rdd =>
      rdd.cache()
      val df: DataFrame = rdd.toDF()
      df.write.mode(SaveMode.Append)
        .option("batchsize", "2000")
        .option("isolationLevel", "NONE") // 设置事务
        .option("numPartitions", "3") // 设置并发
        .option("driver", "ru.yandex.clickhouse.ClickHouseDriver")
        .jdbc("jdbc:clickhouse://10.10.30.45:18123/eto", "liqi2", connectionProperty)


      //    rdd.foreach{orderWide=>
      //      MyKafkaSink.send("DWS_ORDER_WIDE",  JSON.toJSONString(orderWide,new SerializeConfig(true)))
      //
      //    }
      OffsetManager.saveOffset(sdkInfoTopic, sdkInfoGroupId, sdkInfoOffsetRanges)

    }
    ssc.start()
    ssc.awaitTermination()
  }
}
