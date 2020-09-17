package com.etocrm.sdk.sdkserver.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.BigDecimalCodec;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.etocrm.sdk.sdkserver.config.KafkaConfig;
import com.etocrm.sdk.sdkserver.service.KafkaProducerService;
import com.etocrm.sdk.sdkserver.utils.SubLoggerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.MurmurHash;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * 按照单节点模式的模式对Stream模式进行消费处理 (XREAD)
 *
 */
@Component
public class SingleStreamReceiver implements ApplicationListener<ApplicationStartedEvent> {
    public static final String SINGLE_STREAM_MQ_KEY = "STREAM_QUEUE";
    public static final String TEST_STREAM_KEY="liqi_stream";
    private static final String OFFSET_KEY="OFFSET_KEY";
    //    private static final Logger LOG = LoggerFactory.getLogger(SingleStreamReceiver.class);
    private static final Logger LOG = SubLoggerUtils.Logger(SubLoggerUtils.LogFileName.BAITIAO_USER);

    @Autowired
    private KafkaConfig kConfig;

    @Autowired
    private KafkaProducerService kService; //kafka的producer端

    @Autowired
    private JedisPool jedisPool;//目前根据场景来说是单机模式使用

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        LOG.info("RedisConnectionFactory = " + redisConnectionFactory.toString());
        // 创建配置对象
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                // 一次性最多拉取多少条消息
                .batchSize(10)
                // 消息消费异常的handler, 默认写出到 log
//                .errorHandler(Throwable::printStackTrace)
                .errorHandler(throwable -> LOG.error(throwable.getMessage()))
                .build();
        // 根据配置对象创建监听容器
        return StreamMessageListenerContainer
                .create(redisConnectionFactory, streamMessageListenerContainerOptions);
    }

    @Bean
    public StreamListener<String, MapRecord<String, String, String>> streamListener(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer
    ) {
        // 监听器
//        StreamListener<String, MapRecord<String, String, String>> listener = message -> message.forEach(System.out::println);
        StreamListener<String, MapRecord<String, String, String>> listener = message -> {
            LOG.info("MessageID: " + message.getId());
            Map<String, String> r_data = message.getValue();
            LOG.info("Body: " + r_data);
            //集成Kafka写入模块
            String real_value = r_data.get("r_data");
            JSONObject mapTypes = JSON.parseObject(real_value, JSONObject.class);
            String uu = mapTypes.getString("uu");//取到用户uu字段来进行
            //转换数据格式 flat处理
            JSONObject newObject = exchangeFlatValue(mapTypes);
            //计算分区号 
            int partition_random = (int) (Math.abs(MurmurHash.MURMUR_HASH.hash(uu)) % 3);//这里暂时写死应该是动态根据kafka的partition变化进行调整
            kService.sendMessageAsync(kConfig.getTopic(), JSONObject.toJSONString(newObject, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.QuoteFieldNames), partition_random, uu);
//            try {
//                kservice.sendMessageSync(kconfig.getTopic(), real_value);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (TimeoutException e) {
//                e.printStackTrace();
//            }
//            LOG.info("写入kafka成功");
        };
        // 使用监听容器监听消息，并且自动应答
        streamMessageListenerContainer.receive(StreamOffset.create(SINGLE_STREAM_MQ_KEY, ReadOffset.lastConsumed()),
                listener);
        //streamMessageListenerContainer.receive(StreamOffset.create(TEST_STREAM_KEY,getCommitOffset(OFFSET_KEY)),listener);
        return listener;
    }

    //手动维护streaming的offset
    public  ReadOffset  getCommitOffset(String streamKey){
        Jedis jedis = null;
        String offset= null;
        try {
            jedis = jedisPool.getResource();
            Jedis finalJedis = jedis;
            offset=jedis.get(streamKey);
        } catch (JedisException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            returnToPool(jedis);
            if("".equals(offset) || offset.isEmpty()){
                return ReadOffset.lastConsumed();
            }else {
                return ReadOffset.from(offset);
            }
        }
    }
    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    private JSONObject exchangeFlatValue(JSONObject mapTypes) {
        JSONObject obj = new JSONObject();
        //转换ui
        if (mapTypes.get("ui") != null) {
            JSONObject ui = (JSONObject) mapTypes.get("ui");
            obj.put("avatarurl", ui.get("avatarUrl"));
            obj.put("city", ui.get("city"));
            obj.put("country", ui.get("country"));
            obj.put("gender", ui.get("gender"));
            obj.put("language", ui.get("language"));
            obj.put("nickname", ui.get("nickName"));
            obj.put("province", ui.get("province"));
        } else {
            obj.put("avatarurl", null);
            obj.put("city", null);
            obj.put("country", null);
            obj.put("gender", null);
            obj.put("language", null);
            obj.put("nickname", null);
            obj.put("province", null);
        }
        //转换si
        if (mapTypes.get("si") != null) {
            JSONObject si = (JSONObject) mapTypes.get("si");
            obj.put("model", si.get("model"));
            obj.put("brand", si.get("brand"));
            obj.put("pixelratio", si.get("pixelRatio"));
            obj.put("screenwidth", si.get("screenWidth"));
            obj.put("screenheight", si.get("screenHeight"));
            //language与ui中一致，可以重复写入，保证该值能够有值
            obj.put("language", si.get("language"));
            obj.put("version", si.get("version"));
            obj.put("system", si.get("system"));
            obj.put("platform", si.get("platform"));
            obj.put("fontsizesetting", si.get("fontSizeSetting"));
            obj.put("sdkversion", si.get("SDKVersion"));
        } else {
            obj.put("model", null);
            obj.put("brand", null);
            obj.put("pixelratio", null);
            obj.put("screenwidth", 0);
            obj.put("screenheight", 0);
            //language与ui中一致，可以重复写入，保证该值能够有值
            obj.put("language", null);
            obj.put("version", null);
            obj.put("system", null);
            obj.put("platform", null);
            obj.put("fontsizesetting", 0);
            obj.put("sdkversion", null);
        }
        //转换nw
        if (mapTypes.get("nw") != null) {
            JSONObject nw = (JSONObject) mapTypes.get("nw");
            obj.put("networktype", nw.get("networkType"));
        } else {
            obj.put("networktype", null);
        }
        //转换l
        if (mapTypes.get("l") != null) {
            JSONObject l = (JSONObject) mapTypes.get("l");
            obj.put("latitude", l.get("latitude"));
            obj.put("longitude", l.get("longitude"));
            obj.put("speed", l.getBigDecimal("speed"));
            obj.put("accuracy", l.getBigDecimal("accuracy"));
            obj.put("altitude", l.getBigDecimal("altitude"));
            obj.put("verticalaccuracy", l.getBigDecimal("verticalAccuracy"));
            obj.put("horizontalaccuracy", l.getBigDecimal("horizontalAccuracy"));
        } else {
            obj.put("latitude", 0.0);
            obj.put("longitude", 0.0);
            obj.put("speed", 0.0);
            obj.put("accuracy", 0.0);
            obj.put("altitude", 0.0);
            obj.put("verticalaccuracy", 0.0);
            obj.put("horizontalaccuracy", 0.0);
        }
        //转换ao
        //转换q  暂时不处理
        obj.put("q", mapTypes.get("q"));
        //转换te
        obj.put("te", mapTypes.get("te"));
        obj.put("sharefrom", null);
        obj.put("sharetitle", null);
        obj.put("sharepath", null);
        //增加共享页面的维度抽取
        if (mapTypes.getString("tv").equals("page") && mapTypes.getString("tl").equals("share")) {
            JSONArray teArray = mapTypes.getJSONArray("te");
            if (teArray != null) {
                teArray.forEach(data -> {
                    JSONObject or = (JSONObject) data;
                    if (or.getString("k").equals("sharefrom"))
                        obj.put("sharefrom", or.get("v"));
                    else if (or.getString("k").equals("sharetitle"))
                        obj.put("sharetitle", or.get("v"));
                    else if (or.getString("k").equals("path"))
                        obj.put("sharepath", or.get("v"));
                });
            }
        }
        //转换u
        if (mapTypes.get("u") != null) {
            JSONObject u = (JSONObject) mapTypes.get("u");
            obj.put("o", u.get("o"));
            obj.put("u", u.get("u"));
            obj.put("m", u.get("m"));
        } else {
            obj.put("o", null);
            obj.put("u", null);
            obj.put("m", null);
        }
        obj.put("p", mapTypes.get("p"));//page
        obj.put("sourcename", mapTypes.get("SourceName"));

        obj.put("tv", mapTypes.get("tv"));
        obj.put("tl", mapTypes.get("tl"));
        obj.put("reqip", mapTypes.get("ReqIP"));

        obj.put("b", mapTypes.get("b")); //周期ID
        obj.put("k", mapTypes.get("k"));//appkey
        obj.put("uu", mapTypes.get("uu"));//我们自己生成的uuid
        obj.put("t", timeStamp2Date(mapTypes.getLong("t").longValue()));//时间戳转换
        obj.put("c", mapTypes.get("c"));//本轮会话track计数
        obj.put("v", mapTypes.get("v"));//版本信息
        //这次新增表示新增
        obj.put("isfirstday", (mapTypes.getBoolean("ifs") == null) ? false : mapTypes.getBoolean("ifs"));//默认值是false

        return obj;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        LOG.info("SingleStreamReceiver start ==============" + applicationStartedEvent.toString());
        // 启动redis stream 监听
        applicationStartedEvent.getApplicationContext()
                .getBeanProvider(StreamMessageListenerContainer.class)
                .ifAvailable(container -> container.start());
    }


    public String timeStamp2Date(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//要转换的时间格式
        Date date;
        try {
            date = sdf.parse(sdf.format(time));
            return sdf.format(date);
        } catch (Exception e) {
            return null;
        }
    }
}
