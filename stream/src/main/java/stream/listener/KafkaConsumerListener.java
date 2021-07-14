package stream.listener;


import com.alibaba.fastjson.JSONObject;
import com.eto.sdk.stream.base.Constants;
import com.eto.sdk.stream.entity.SdkNew;
import com.eto.sdk.stream.service.SdkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

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

    @Autowired
    private JedisPool jedisPool;//目前根据场景来说是单机模式使用




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
            topicPartitions = {@TopicPartition(topic ="${spring.kafka.topic}", partitions = {"0", "1", "2"})},
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "concurrentKafkaListenerContainerBatchFactory")
    public void listen(List<ConsumerRecord<String, String>> recordList) {
     long start=System.currentTimeMillis();
    List<SdkNew> list = new ArrayList<>();
    //log.info("====size==="+recordList.size());
    for (ConsumerRecord<String, String> record : recordList) {
        String mValue = record.value();
        list.add(JSONObject.parseObject(record.value(), SdkNew.class));
    }
//    Jedis jedis = null;
//    try {
//       jedis = jedisPool.getResource();
//        Jedis finalJedis = jedis;
//        for (int i = 0; i < list.size(); i++) {
//            if (Constants.APP.equals(list.get(i).getTv()) && Constants.SHOW.equals(list.get(i).getTl())) {
//                if (!hasKey(Constants.USER_ID_BIT_SET_Test, list.get(i).getUu(), finalJedis)) {
//                    list.get(i).setIsfirstday(1);//标明是新用户
//                    addKey(Constants.USER_ID_BIT_SET_Test, list.get(i).getUu(), finalJedis);
//                }
//            }
//        }
//    }catch (JedisException e){
//        log.error(e.getMessage(), e);
//        //log.error("insert redis boolean  error record=" + JSONArray.toJSONString(arry));
//    }finally {
//        returnToPool(jedis);
//    }
        //System.out.println(Arrays.toString(list.toArray()));
        sdkService.insertBatch(list);
        long end=System.currentTimeMillis();
        log.info("===========耗时=============" + (end - start));
    }

    /**
     * 添加元素到bitSet中
     * 在bitset中设置key和value
     *
     * @param key
     */
    public void addKey(String key, String uu, Jedis jedis) {
        for (int i : Constants.primeNums) {
            // 计算hashcode
            int hashcode = hash(uu, i);
            // 计算映射在bitset上的位置
            int bitIndex = hashcode & (Constants.length - 1);
            // setBloomFilterKey(USER_ID_BIT_SET, bitIndex, true ,jedis);
            try {
                jedis.setbit(key, bitIndex, true);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 判断bitSet中是否有被查询的的key(经过hash处理之后的)
     *
     * @param key
     * @return
     */
    public boolean hasKey(String key, String uu, Jedis jedis) {
        for (int i : Constants.primeNums) {
            // 计算hashcode
            int hashcode = hash(uu, i);
            // 计算映射在bitset上的位置
            int bitIndex = hashcode & (Constants.length - 1);
            // 只要有一个位置对应不上，则返回false
            if (!getBloomFilterValue(key, bitIndex, jedis)) {
                return false;
            }
        }
        return true;
    }


    /** 布隆过滤器 **/
    /**
     * 根据索引从bitmap中获取值
     *
     * @param bitIndex bitset的索引值
     * @return
     */
    public boolean getBloomFilterValue(String key, int bitIndex, Jedis jedis) {
        boolean flag = false;
        try {
            flag = jedis.getbit(key, bitIndex);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

        }
        return flag;
    }


    /**
     * 自定义hash函数
     *
     * @param key
     * @param prime
     * @return
     */
    private static int hash(String key, int prime) {
        int h = 0;
        char[] value = key.toCharArray();
        if (h == 0 && value.length > 0) {
            char val[] = value;
            for (int i = 0; i < value.length; i++) {
                h = prime * h + val[i];
            }
        }
        return h;
    }
    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

}