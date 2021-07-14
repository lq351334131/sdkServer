package server.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.etocrm.sdk.server.base.Constants;
import com.etocrm.sdk.server.base.JsonFeedback;
import com.etocrm.sdk.server.base.ResponseCode;
import com.etocrm.sdk.server.config.KafkaConfig;
import com.etocrm.sdk.server.utils.SdkDateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.MurmurHash;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * 处理上报数据，临时存入reids缓冲队列中
 */

@Service
public class TrackLogService {


    @Autowired
    private KafkaProducerService kafkaProducerService;//目前根据场景来说是单机模式使用

    @Autowired
    private KafkaConfig kConfig;

    private static final Logger log = LoggerFactory.getLogger(TrackLogService.class);

    public JsonFeedback sendKafka(JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject o = (JSONObject) jsonArray.get(i);
                JSONObject mapTypes = exchangeFlatValue(o);
                   // System.out.println("sss"+mapTypes.toString());
                    String uu = mapTypes.getString("uu");//取到用户uu字段来进行
                    //转换数据格式 flat处理
                    //计算分区号
                    int partition_random = (int) (Math.abs(MurmurHash.MURMUR_HASH.hash(uu)) % 3);//这里暂时写死应该是动态根据kafka的partition变化进行调整
                    //log.info(mapTypes.toJSONString());
                    kafkaProducerService.sendMessageAsync(kConfig.getTopic(), JSONObject.toJSONString(mapTypes, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.QuoteFieldNames), partition_random, uu);
                }
            return new JsonFeedback(ResponseCode.OK);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("error msg"+e.getMessage());
            return new JsonFeedback(ResponseCode.Kafka_Connect_ExCeption);
        }

    }

    /**
     * 添加元素到bitSet中
     * 在bitset中设置key和value
     */
    public JSONObject exchangeFlatValue(JSONObject mapTypes) {

        JSONObject obj = new JSONObject();
        //obj.put("sourcename", mapTypes.get("SourceName"));
        if ( "".equals(mapTypes.getString("tl"))) {//当前track页面事件类型: show hide  share error 404 eventId
            obj.put("tl", "info");
        } else {
            obj.put("tl",mapTypes.get("tl"));
        }
        obj.put("tv", mapTypes.get("tv"));//当前track发起环境: qpp page event/
        obj.put("retry", mapTypes.get("rt"));//如果是初次发的 rt就是0
        obj.put("back", mapTypes.get("b")); //周期ID
        obj.put("appkey", mapTypes.get("k"));//appkey
        obj.put("uu", mapTypes.get("uu"));//我们自己生成的uuid
        obj.put("t", mapTypes.get("t"));//获取时间戳
        obj.put("time", timeStamp2Date(mapTypes.getLong("t").longValue()));//时间戳转换
        obj.put("dt", getDay(mapTypes.getLong("t").longValue()));//增加分区字段
        obj.put("tt", getTTDay(System.currentTimeMillis()));//增加处理时间 用来校验事件时间  （sdk存在迟到数据）
        obj.put("track", mapTypes.get("c"));//本轮会话track计数
        //obj.put("v", mapTypes.get("v"));//版本信息
        //转换ai

        //转换ai
        if (mapTypes.containsKey("ai") && mapTypes.getJSONObject("ai") !=null) {
            JSONObject ai = mapTypes.getJSONObject("ai").getJSONObject("miniProgram");
            if(ai!=null && ai.size()>0){

                if(ai.get("appId") ==null || "".equals(ai.get("appId"))){
                    obj.put("appId", "un");
                }else{
                    obj.put("appId", ai.get("appId"));
                }

                if(ai.get("envVersion") ==null || "".equals(ai.get("envVersion"))){
                    obj.put("enVersion", "un");
                }else{
                    obj.put("enVersion", ai.get("envVersion"));
                }

                if(ai.get("version") ==null || "".equals(ai.get("version"))){
                    obj.put("version", "un");
                }else{
                    obj.put("version", ai.get("version"));
                }
           }else {
                obj.put("appId", "un");
                obj.put("enVersion","un");
                obj.put("version", "un");
            }
        } else {
            obj.put("appId", "un");
            obj.put("enVersion","un");
            obj.put("version", "un");
        }

        obj.put("entryPage", 0);//入口页
        //关于小程序场景的判定
        obj.put("appShow",0);
        obj.put("pageShow",0);
        obj.put("pageShare",0);
        obj.put("appHide",0);
        obj.put("pageHide",0);
        obj.put("eventClick",0);

        //电商4个事件上报的判定
        obj.put("getShop",0);
        obj.put("addToCart", 0);
        obj.put("customOrder", 0);
        obj.put("search", 0);

        //对于小程序动态列参数的解析
        obj.put("paramTe", "un");
        obj.put("paramQ", "un");


        //转换u
        if (mapTypes.containsKey("u")  &&  mapTypes.getJSONObject("u") !=null) {
            JSONObject u = (JSONObject) mapTypes.get("u");
            if(u!=null && u.size()>0){
                obj.put("openId", u.get("o"));
                obj.put("unionId", u.get("u"));
                //obj.put("mobiePhone","unknow");
            }else {
                obj.put("openId", "un");
                obj.put("unionId", "un");
                //obj.put("mobiePhone","unknow");
            }
        } else {
            obj.put("openId", "un");
            obj.put("unionId", "un");
           //obj.put("mobiePhone","unknow");


        }

//        //转换ui
//        if (mapTypes.containsKey("ui") && mapTypes.get("ui") != null) {
//            JSONObject ui = (JSONObject) mapTypes.get("ui");
//            // obj.put("avatarurl", ui.get("avatarUrl"));
//            obj.put("city", ui.get("city"));
//            obj.put("country", ui.get("country"));
//            obj.put("gender", ui.get("gender"));
//            obj.put("language", ui.get("language"));
//            obj.put("nickname", ui.get("nickName"));
//            obj.put("province", ui.get("province"));
//        } else {
//            //obj.put("avatarurl", null);
//            obj.put("city", null);
//            obj.put("country", null);
//            obj.put("gender", 3);
//            obj.put("language", null);
//            obj.put("nickname", null);
//            obj.put("province", null);
//        }



        if (mapTypes.get("ReqIP") != null) {
            obj.put("reqIp", mapTypes.get("ReqIP").toString());//真实ip地址
        } else {
            obj.put("reqIp", "0.0.0.0.1");//真实ip地址
        }
        //转换si si nw l api报错的时候都会给null
        if (mapTypes.containsKey("si") && mapTypes.getJSONObject("si")!=null) {
            JSONObject si = (JSONObject) mapTypes.get("si");
            obj.put("model", si.get("model"));
            obj.put("brand", si.get("brand"));
            //obj.put("pixelratio", si.get("pixelRatio"));
            //obj.put("screenwidth", si.get("screenWidth"));
            //obj.put("screenheight", si.get("screenHeight"));
            //language与ui中一致，可以重复写入，保证该值能够有值
            //obj.put("language", si.get("language"));
            // obj.put("version", si.get("version"));
            obj.put("systems", si.get("system"));
            obj.put("platForm", si.get("platform"));
            //obj.put("fontsizesetting", si.get("fontSizeSetting"));
            // obj.put("sdkversion", si.get("SDKVersion"));
        } else {
            obj.put("model", "un");
            obj.put("brand", "un");
            //obj.put("pixelratio","unknow");
            // obj.put("screenwidth", 0);
            // obj.put("screenheight", 0);
            //language与ui中一致，可以重复写入，保证该值能够有值
            //obj.put("language", "un");
            // obj.put("version", "");
            obj.put("systems", "un");
            obj.put("platForm", "un");
            //obj.put("fontsizesetting", 0);
            //obj.put("sdkversion", null);
        }

        //转换nw
        if (mapTypes.containsKey("nw") && mapTypes.get("nw") != null) {
            JSONObject nw = (JSONObject) mapTypes.get("nw");
            obj.put("networktype", nw.get("networkType"));
        } else {
            obj.put("networktype", "un");
        }

        //根据插入的当前时间 是本周 本月的第几天
        if (mapTypes.get("t") != null) {
            Map<String, Integer> dateMap = SdkDateUtils.getWeekMonthYear(mapTypes.getLong("t").longValue());
            obj.put("year", dateMap.get("year"));
            obj.put("quarter", dateMap.get("quarter"));
            obj.put("month", dateMap.get("month"));
            // obj.put("dayofweek", dateMap.get("weekDay"));
            //obj.put("dayofmonth", dateMap.get("monthDay"));
        } else {
            obj.put("year", 0);
            obj.put("quarter", 0);
            obj.put("month", 0);
            //obj.put("dayOfWeek", 0);
            //obj.put("dayOfMonth", 0);
        }

        //转换l
        if (mapTypes.containsKey("l") && mapTypes.get("l") != null) {
            JSONObject l = (JSONObject) mapTypes.get("l");
            obj.put("latitude", l.get("latitude"));
            obj.put("longitude", l.get("longitude"));
            //obj.put("speed", l.getBigDecimal("speed"));
            // obj.put("accuracy", l.getBigDecimal("accuracy"));
            //obj.put("altitude", l.getBigDecimal("altitude"));
            // obj.put("verticalaccuracy", l.getBigDecimal("verticalAccuracy"));
            // obj.put("horizontalaccuracy", l.getBigDecimal("horizontalAccuracy"));
        } else {
            obj.put("latitude", 0.0);
            obj.put("longitude", 0.0);
            // obj.put("speed", 0.0);
            // obj.put("accuracy", 0.0);
            //obj.put("altitude", 0.0);
            //obj.put("verticalaccuracy", 0.0);
            // obj.put("horizontalaccuracy", 0.0);
        }

        //转换ao  app-onshow-option 每次打开小程序的入口页
        if (mapTypes.containsKey("ao") && mapTypes.getJSONObject("ao")!=null) {
            JSONObject ao = mapTypes.getJSONObject("ao");
            if(ao != null && ao.size()>0){
                obj.put("path", ao.get("path"));
                obj.put("entryPage", 1);//入口页
                obj.put("scene", ao.get("scene"));
                // obj.put("referrerinfo", ao.get("referrerinfo"));
                if (ao.getJSONArray("query")!=null && ao.getJSONArray("query").size() >0) {
                    obj.put("query", ao.getJSONArray("query").toString());
                } else {
                    obj.put("query", "un");
                }
            }else{
                obj.put("path", "un");
                obj.put("scene", -1);
                // obj.put("referrerinfo", null);
                obj.put("query", "un");
            }
        } else {
            obj.put("path", "un");
            obj.put("scene", -1);
            //obj.put("referrerinfo", null);
            obj.put("query", "un");
        }


        //转换q  暂时不处理  q是页面路径带的参数 是一个动态字段
        if (mapTypes.containsKey("q") && mapTypes.getJSONArray("q") !=null) {
            JSONArray qprarmArray = mapTypes.getJSONArray("q");
            if(qprarmArray !=null && qprarmArray.size()>0){
                JSONObject qprarm = new JSONObject();
                obj.put("q",qprarmArray.toString());
                //obj.put("q", mapTypes.getJSONArray("q").toString());
                qprarmArray.forEach(data -> {
                    JSONObject or = (JSONObject) data;
                    qprarm.put(or.getString("k"), or.get("v"));
                    obj.put("paramQ", qprarm.toJSONString());
                });
            }else{
                obj.put("q", "un");
                obj.put("paramQ", "un");
            }
        } else {
            obj.put("q", "un");
            obj.put("paramQ", "un");
        }

        //转换te
        if (mapTypes.containsKey("te") && mapTypes.getJSONArray("te")!=null) {
            JSONArray teArray = mapTypes.getJSONArray("te");
            if(teArray!=null && teArray.size()>0){
                JSONObject te = new JSONObject();
                obj.put("te", teArray.toString());
                teArray.forEach(data -> {
                    JSONObject or = (JSONObject) data;
                    te.put(or.getString("k"), or.get("v"));
                });
                obj.put("paramTe", te.toJSONString());
            }else {
                obj.put("te", "un");
                obj.put("paramTe", "un");
            }
        }else{
            obj.put("te", "un");
            obj.put("paramTe", "un");
        }

        if (mapTypes.containsKey("p") && mapTypes.get("p") != null ) { //当前track页面的path
            String p =mapTypes.getString("p");
            if(p!=null && p.length()>0){
                obj.put("visitPath", mapTypes.get("p") );
            }else{
                obj.put("visitPath", "un");
            }
        } else {
            obj.put("visitPath", "un");
        }



        //TODO  指标计算逻辑
        if (Constants.APP.equals(mapTypes.get("tv")) && Constants.SHOW.equals(mapTypes.get("tl"))) {
            obj.put("appShow", 1);
        }

        if (Constants.PAGE.equals(mapTypes.get("tv")) && Constants.SHOW.equals(mapTypes.get("tl"))) {
            obj.put("pageShow", 1);
        }
        if (Constants.APP.equals(mapTypes.get("tv")) && Constants.HIDE.equals(mapTypes.get("tl"))) {
            obj.put("appHide",1);
        }

        if (Constants.PAGE.equals(mapTypes.get("tv")) && Constants.HIDE.equals(mapTypes.get("tl"))) {
            obj.put("pageHide",1);
        }

        //此条数据专门用来上报用户信息的 与具体的操作无关
        if (Constants.EVENT.equals(mapTypes.get("tv"))  && "".equals(mapTypes.get("tl")))  {
            obj.put("eventClick", 2);
        }
        if (Constants.EVENT.equals(mapTypes.get("tv"))  && Constants.GETSHOP.equals(mapTypes.get("tl") ) ) {
            obj.put("getShop", 1);
        }
        if (Constants.EVENT.equals(mapTypes.get("tv"))  && Constants.ADDTOCART.equals(mapTypes.get("tl") ) ) {
            obj.put("addToCart", 1);
        }
        if (Constants.EVENT.equals(mapTypes.get("tv"))  && Constants.CUSTOMORDER.equals(mapTypes.get("tl") ) ) {
            obj.put("customOrder", 1);
        }
        if (Constants.EVENT.equals(mapTypes.get("tv"))  && Constants.SEARCH.equals(mapTypes.get("tl") ) ) {
            obj.put("search", 1);
        }


        obj.put("shareFrom", "un");
        obj.put("shareTitle", "un");
        obj.put("sharePath", "un");
        //增加共享页面的维度抽取
        if (Constants.PAGE.equals(mapTypes.get("tv")) && Constants.SHARE.equals(mapTypes.get("tl"))) {
            JSONArray teArray = mapTypes.getJSONArray("te");
            if (teArray!=null && teArray.size()>0) {
                teArray.forEach(data -> {
                    JSONObject or = (JSONObject) data;
                    if (Constants.SHAREFROM.equals(or.getString("k") ))
                        obj.put("shareFrom", or.get("v"));
                    else if (Constants.SHARETITLE.equals(or.getString("k")))
                        obj.put("shareTitle", or.get("v"));
                    else if (Constants.SHAREPATH.equals(or.getString("k")))
                        obj.put("sharePath", or.get("v"));
                });
            }
            //obj.put("sharetime", 1);
            obj.put("pageShare", 1);
        }


        //增加新入会指标
        obj.put("isNewMember", 0);
        if (Constants.EVENT.equals(mapTypes.get("tv"))  && Constants.CLICK.equals(mapTypes.get("tl") ) ) {
            JSONArray teArray = mapTypes.getJSONArray("te");
            if (teArray != null) {
                teArray.forEach(data -> {
                    JSONObject or = (JSONObject) data;
                    if (Constants.INTO_MEMBER.equals(or.getString("te")))
                        obj.put("isNewMember",1);
                });
            }
            obj.put("eventClick", 1);
        }
        return obj;
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

//    public static void main(String[] args) {
//        Boolean flag=true;
//        for (int i = 0; i <3 ; i++) {
//            if(i==1){
//                flag =false;
//            }
//        }
//        if(!flag){
//
//            System.out.println("aaaaa");
//        }
//
//    }

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

    public static String getTTDay(Long time) {
        Date date = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(date);
    }


}
