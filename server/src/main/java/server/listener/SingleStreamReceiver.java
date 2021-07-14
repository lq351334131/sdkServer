package server.listener;//package com.etocrm.sdk.server.listener;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//
//import com.etocrm.sdk.server.base.Constants;
//import com.etocrm.sdk.server.config.KafkaConfig;
//import com.etocrm.sdk.server.service.KafkaProducerService;
//import com.etocrm.sdk.server.utils.IpAddressUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.context.event.ApplicationStartedEvent;
//import org.springframework.context.ApplicationListener;
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.stream.MapRecord;
//import org.springframework.data.redis.connection.stream.ReadOffset;
//import org.springframework.data.redis.connection.stream.StreamOffset;
//import org.springframework.data.redis.stream.StreamListener;
//import org.springframework.data.redis.stream.StreamMessageListenerContainer;
//import org.springframework.stereotype.Component;
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisPool;
//import redis.clients.jedis.exceptions.JedisException;
//import redis.clients.jedis.util.MurmurHash;
//
//import java.util.Map;
//
///**
// * 按照单节点模式的模式对Stream模式进行消费处理 (XREAD)
// *
// */
//@Component
//@Slf4j
//public class SingleStreamReceiver implements ApplicationListener<ApplicationStartedEvent> {
//
//
//
//    public static final String TEST_STREAM_KEY = "liqi_stream1";
//    private static final String OFFSET_KEY = "OFFSET_KEY";
//    //    private static final Logger LOG = LoggerFactory.getLogger(SingleStreamReceiver.class);
//    //private static final Logger LOG = SubLoggerUtils.Logger(SubLoggerUtils.LogFileName.BAITIAO_USER);
//
//    @Value("${redis_stream}")
//    private String SINGLE_STREAM_MQ_KEY;
//
//    @Autowired
//    private KafkaConfig kConfig;
//
//    @Autowired
//    private KafkaProducerService kService; //kafka的producer端
//
//    @Autowired
//    private JedisPool jedisPool;//目前根据场景来说是单机模式使用
//
//    @Bean
//    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
//        log.info("RedisConnectionFactory = " + redisConnectionFactory.toString());
//        // 创建配置对象
//        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
//                .builder()
//                // 一次性最多拉取多少条消息
//                .batchSize(300)
//                // 消息消费异常的handler, 默认写出到 log
////                .errorHandler(Throwable::printStackTrace)
//                .errorHandler(throwable -> log.error(throwable.getMessage()))
//                .build();
//        // 根据配置对象创建监听容器
//        return StreamMessageListenerContainer
//                .create(redisConnectionFactory, streamMessageListenerContainerOptions);
//    }
//
//    @Bean
//    public StreamListener<String, MapRecord<String, String, String>> streamListener(
//            StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer
//    ) {
//        // 监听器
////        StreamListener<String, MapRecord<String, String, String>> listener = message -> message.forEach(System.out::println);
//        StreamListener<String, MapRecord<String, String, String>> listener = message -> {
//            //LOG.info("MessageID: " + message.getId());
//            Map<String, String> r_data = message.getValue();
//            //LOG.info("Body: " + r_data);
//            //集成Kafka写入模块
//            String real_value = r_data.get("r_data");
//            JSONObject mapTypes = JSON.parseObject(real_value, JSONObject.class);
//            String uu = mapTypes.getString("uu");//取到用户uu字段来进行
//            //转换数据格式 flat处理
//             JSONObject newObject = getLocalByIp(mapTypes);
//            //计算分区号
//            int partition_random = (int) (Math.abs(MurmurHash.MURMUR_HASH.hash(uu)) % 3);//这里暂时写死应该是动态根据kafka的partition变化进行调整
//            kService.sendMessageAsync(kConfig.getTopic(), JSONObject.toJSONString(mapTypes, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.QuoteFieldNames), partition_random, uu);
////            try {
////                kservice.sendMessageSync(kconfig.getTopic(), real_value);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            } catch (ExecutionException e) {
////                e.printStack；Trace();
////            } catch (TimeoutException e) {
////                e.printStackTrace();
////            }
////            LOG.info("写入kafka成功");
//        };
//        // 使用监听容器监听消息，并且自动应答
//        streamMessageListenerContainer.receive(StreamOffset.create(SINGLE_STREAM_MQ_KEY, ReadOffset.lastConsumed()),
//                listener);
//        //streamMessageListenerContainer.receive(StreamOffset.create(TEST_STREAM_KEY,getCommitOffset(OFFSET_KEY)),listener);
//        return listener;
//    }
//
//    private JSONObject getLocalByIp(JSONObject mapTypes) {
//        JSONObject obj = new JSONObject();
//        if(Constants.APP.equals(mapTypes.get("tv")) && Constants.SHOW.equals("tl")) {
//            String ip = mapTypes.get("ReqIP").toString();
//            Map ipMap = IpAddressUtils.getIpWs126Net2(ip);
//            if (!ipMap.isEmpty()) {
//                obj.put("province", ipMap.get("province"));
//                obj.put("city", ipMap.get("city"));
//            }
//        }
//        return obj;
//    }
//
//    //手动维护streaming的offset
//    public ReadOffset getCommitOffset(String streamKey) {
//        Jedis jedis = null;
//        String offset = null;
//        try {
//            jedis = jedisPool.getResource();
//            Jedis finalJedis = jedis;
//            offset = jedis.get(streamKey);
//        } catch (JedisException e) {
//            log.error(e.getMessage(), e);
//        } finally {
//            returnToPool(jedis);
//            if ("".equals(offset) || offset.isEmpty()) {
//                return ReadOffset.lastConsumed();
//            } else {
//                return ReadOffset.from(offset);
//            }
//        }
//    }
//
//    private void returnToPool(Jedis jedis) {
//        if (jedis != null) {
//            jedis.close();
//        }
//    }
//
//
//
//
//
//    @Override
//    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
//        log.info("SingleStreamReceiver start ==============" + applicationStartedEvent.toString());
//        // 启动redis stream 监听
//        applicationStartedEvent.getApplicationContext()
//                .getBeanProvider(StreamMessageListenerContainer.class)
//                .ifAvailable(container -> container.start());
//    }
//
//}