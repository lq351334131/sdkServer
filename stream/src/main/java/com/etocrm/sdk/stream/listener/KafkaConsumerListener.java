package com.etocrm.sdk.stream.listener;


import com.alibaba.fastjson.JSONObject;
import com.etocrm.sdk.stream.entity.Salary;
import com.etocrm.sdk.stream.entity.Sdk;
import com.etocrm.sdk.stream.service.SdkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author qi.li
 * @create 2020/11/9 17:25
 *
 * kafka监听消费
 * 1,单条消费，多个消费者群组可以共同读取同一个主题，彼此之间互不影响
 * 2,批量消费
 */
@Component
@Slf4j
public class KafkaConsumerListener {

    @Autowired
    private SdkService sdkService;


//    /**
//     * 单条消费
//     */
//    @KafkaListener(id = "consumer1-1",
//            containerFactory = "concurrentKafkaListenerContainerFactory",
//            groupId = "${spring.kafka.consumer.group-id}",
//            topicPartitions = {@TopicPartition(topic = "${spring.kafka.topic}", partitions = {"0", "1", "2"})})
//    public void consumer1_1(ConsumerRecord<String, String> record) {
//        String mValue = record.value();
//        log.info("consumer收到消息VALUE:" + mValue);
//        log.info("consumer收到消息KEY:" + record.key());
//       // sdkService.insertSingle(mValue);
//    }




    /**
     * 批量消费
     */
@KafkaListener(id = "myListener",
            topicPartitions = {@TopicPartition(topic = "${spring.kafka.topic}", partitions = {"0", "1", "2"})},
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "concurrentKafkaListenerContainerBatchFactory")
    public void listen(List<ConsumerRecord<String, String>> recordList) {
        List<Salary> list = new ArrayList<>();
        long start=System.currentTimeMillis();
        log.info("=====start======"+start);
        log.info("====size==="+recordList.size());
        for (ConsumerRecord<String, String> record : recordList) {
             String mValue = record.value();
            log.info(record.key());
            log.info(record.value());
            list.add(JSONObject.parseObject(record.value(), Salary.class));
        }
        Long end=System.currentTimeMillis();
       log.info("===========封装实体类大小============="+(list.size()));
        sdkService.insertBatch(list);
       log.info("===========end============="+(end));
        log.info("===========耗时============="+(end-start));
    }

}