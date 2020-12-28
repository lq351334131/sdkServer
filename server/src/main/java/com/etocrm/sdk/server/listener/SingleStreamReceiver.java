package com.etocrm.sdk.server.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import com.etocrm.sdk.server.config.KafkaConfig;
import com.etocrm.sdk.server.service.KafkaProducerService;
import com.etocrm.sdk.server.utils.IpAddressUtils;
import com.etocrm.sdk.server.utils.IpLocationTool;
import com.etocrm.sdk.server.utils.SdkDateUtils;
import com.etocrm.sdk.server.utils.SubLoggerUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.MurmurHash;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 按照单节点模式的模式对Stream模式进行消费处理 (XREAD)
 *
 */
@Component
public class SingleStreamReceiver implements ApplicationListener<ApplicationStartedEvent> {

    private static final String IP_URL = "http://iploc.market.alicloudapi.com/v3/ip";
    public static final String SINGLE_STREAM_MQ_KEY = "STREAM_QUEUE";
    public static final String TEST_STREAM_KEY = "liqi_stream";
    private static final String OFFSET_KEY = "OFFSET_KEY";
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
//                e.printStack；Trace();
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
    public ReadOffset getCommitOffset(String streamKey) {
        Jedis jedis = null;
        String offset = null;
        try {
            jedis = jedisPool.getResource();
            Jedis finalJedis = jedis;
            offset = jedis.get(streamKey);
        } catch (JedisException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            returnToPool(jedis);
            if ("".equals(offset) || offset.isEmpty()) {
                return ReadOffset.lastConsumed();
            } else {
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
        //关于九个新指标
        obj.put("newpage", 0);//新访页
        obj.put("depth", 0);//深度页
        obj.put("entrypage", 0);//入口页
        obj.put("exitpage", 0);//appkey
        obj.put("accesspage", 0);//受访页或者访问页
        obj.put("openapp", 0);//打开次数 不可以计算
        obj.put("pagetime", 0);//页面停留时长  不可以计算
        obj.put("apptime", 0);//app停留时长  不可以计算
        obj.put("sharetime", 0);//分享次数  可以计算



        //TODO  指标计算逻辑
        String tv = mapTypes.getString("tv");
        String tl = mapTypes.getString("tl");
        if (mapTypes.getIntValue("isfirstday") == 1 ) {
            obj.put("newpage", 1);//新访页
        } else if (tv.equals("page") && tl.equals("show")) {
            obj.put("newpage", 0);//新访页
            obj.put("depth", 1);//深度页t'b'b
            obj.put("accesspage", 1);//受访页或者访问页
            obj.put("entrypage", 1);//入口页
        } else if (tv.equals("page") && tl.equals("share")) {
            obj.put("sharetime", 1);//分享次数  可以计算
        } else if (tv.equals("app") && tl.equals("hide")) {
            obj.put("exitpage", 1);
        }
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
            String ip = mapTypes.get("ReqIP").toString();
//            Map ipMap= IpAddressUtils.getIpWs126Net2(ip);
//            if(!ipMap.isEmpty()){
//                obj.put("province", ipMap.get("province"));
//                obj.put("city",ipMap.get("province"));
 //           }

            //获取IP地址通过第三方服务对IP地址获取的信息进行修正   目前需要收费 所以暂时不打开
            //String ipInfo = OkHttpUtil.get(IP_URL, ImmutableMap.of("ip", ip));
//          if (!Strings.isNullOrEmpty(ipInfo)) {
//            JSONObject IPobj = JSON.parseObject(ipInfo);
//            if (IPobj.getString("status").equals("1")) {
//                obj.put("province", IPobj.getString("province")); //修正:省
//                Object jo = IPobj.get("city");
//                if (jo instanceof JSONArray) {
//                    obj.put("city", null);//修正:市
//                } else if (jo instanceof String) {
//                    obj.put("city", IPobj.getString("city"));//修正:市
//                }
//            }
//        }
        }
        if(mapTypes.get("ReqIP") != null){
            String ip2=mapTypes.getString("ReqIP");
            Map<String,String> localMap=IpLocationTool.getCity(ip2);
            obj.put("province",localMap.get("province"));
            obj.put("city",localMap.get("city"));
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
        //转换ao  app-onshow-option 每次打开小程序的入口页
        if (mapTypes.get("ao") != null) {
            JSONObject ao = (JSONObject) mapTypes.get("ao");
            obj.put("path", ao.get("path"));
            obj.put("scene", ao.get("scene"));
            obj.put("referrerinfo", ao.get("referrerinfo"));
            if(ao.getJSONArray("query")!=null){
                obj.put("query", ao.getJSONArray("query").toString());
            }else{
                obj.put("query", null);
            }
        } else {
            obj.put("path", null);
            obj.put("scene", null);
            obj.put("referrerinfo", null);
            obj.put("query", null);
        }
        //转换q  暂时不处理
        if(mapTypes.get("q") != null){
            obj.put("q", mapTypes.getJSONArray("q").toString());
        }else{
            obj.put("q", null);
        }

        //根据插入的当前时间 是本周 本月的第几天
        if (mapTypes.get("t") != null) {
            Map<String, Integer> dateMap = SdkDateUtils.getWeekMonthYear(mapTypes.getLong("t").longValue());
            obj.put("year", dateMap.get("year"));
            obj.put("quarter", dateMap.get("quarter"));
            obj.put("month", dateMap.get("month"));
            obj.put("dayOfWeek", dateMap.get("weekDay"));
            obj.put("dayOfMonth", dateMap.get("monthDay"));
        } else {
            obj.put("year", null);
            obj.put("quarter", null);
            obj.put("month", null);
            obj.put("dayOfWeek", null);
            obj.put("dayOfMonth", null);
        }
        //转换te
        if(mapTypes.get("te") != null){
            obj.put("te", mapTypes.getJSONArray("te").toString());
        }else{
            obj.put("te", null);
        }
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
            obj.put("sharetime", 1);
        }

        //转换u
        if (mapTypes.get("u") != null) {
            JSONObject u = (JSONObject) mapTypes.get("u");
            obj.put("openId", u.get("o"));
            obj.put("unionId",u.get("u"));
            obj.put("mobiePhone", u.get("m"));
        } else {
            obj.put("o", null);
            obj.put("u", null);
            obj.put("m", null);
        }

        obj.put("path", mapTypes.get("p"));//当前track页面的path
        obj.put("sourcename", mapTypes.get("SourceName"));
        obj.put("tv", mapTypes.get("tv"));//当前track发起环境: qpp page event
        obj.put("tl", mapTypes.get("tl"));//当前track页面事件类型: show hide  share error 404 eventId
        obj.put("reqIp", mapTypes.get("ReqIP"));//真实ip地址
        obj.put("back", mapTypes.get("b")); //周期ID
        obj.put("appKey", mapTypes.get("k"));//appkey
        obj.put("uuId", mapTypes.get("uu"));//我们自己生成的uuid
        obj.put("timestamp", mapTypes.get("t"));//获取时间戳
        obj.put("time", timeStamp2Date(mapTypes.getLong("t").longValue()));//时间戳转换
        obj.put("dt", getDay(mapTypes.getLong("t").longValue()));//增加分区字段
        obj.put("c", mapTypes.get("c"));//本轮会话track计数
        obj.put("v", mapTypes.get("v"));//版本信息
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

    public static String getDay(Long time) {
        Date date = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }

    /**
     * @Description 新注册
     * @author xing.liu
     * @date 2020/11/23
     **/
    private Integer getRegUser(Integer isfirstday, JSONObject mapTypes) {
        Integer newPage = 0;
        String tv = mapTypes.getString("tv");
        String tl = mapTypes.getString("tl");
        String uu = mapTypes.getString("uu");
        String k = mapTypes.getString("k");
        String key = "newRegUser:" + k + ":" + uu;
        if (isfirstday == 1 && tv.equals("page") && tl.equals("show")) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                Set<String> isFirstDaySet = jedis.zrange(key, 0, -1);
                int size = isFirstDaySet.size();
                if (size == 0) {
                    jedis.zadd(key, 0, "1");
                    newPage = 1;
                }
            } catch (JedisException e) {

            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return newPage;
    }

    /**
     * @Description入口页
     * @author xing.liu
     * @date 2020/11/23
     **/
    private Integer getEntryUser(JSONObject mapTypes) {
        String tv = mapTypes.getString("tv");
        String tl = mapTypes.getString("tl");
        String uu = mapTypes.getString("uu");
        String k = mapTypes.getString("k");
        String key = "entrypage:" + k + ":";
        String type = tv + "." + tl;
        Integer entryNum = 0;
        if (type.equals("page.show")) {
            String ekey = key + mapTypes.get("uu") + mapTypes.get("b");
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                String eValue = jedis.get(ekey);
                if (StringUtils.isEmpty(eValue)) {
                    jedis.set(ekey, "1");
                    entryNum = 1;
                } else {
                    jedis.set(ekey, "0");
                }
            } catch (JedisException e) {

            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
        return entryNum;
    }

    //读取clickhouse中的用户表用来判断是否新用户
    public Integer getFirstDay(JSONObject mapTypes) {
        if(mapTypes.get("uu")!=null && mapTypes.get("k")!=null){
            String uu=mapTypes.getString("uu");
            String k=mapTypes.getString("k");


        }

          return 1;
    }
}