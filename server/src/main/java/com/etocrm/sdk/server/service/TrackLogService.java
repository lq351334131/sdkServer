package com.etocrm.sdk.server.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.etocrm.sdk.server.base.JsonFeedback;
import com.etocrm.sdk.server.base.ResponseCode;
import com.etocrm.sdk.server.listener.SingleStreamReceiver;
import com.etocrm.sdk.server.utils.SubLoggerUtils;
import com.etocrm.sdk.server.utils.TxtUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


/**
 * 处理上报数据，临时存入reids缓冲队列中
 */
@Service
public class TrackLogService {

    // 布隆过滤器key1
    private static final String USER_ID_BIT_SET = "user_id_strhash_bloomfilter";
    // 初始化集合长度
    private static final int length = Integer.MAX_VALUE;
    // 准备hash计算次数
    private static final int HASH_LENGTH = 5;
    /**
     * 准备自定义哈希算法需要用到的质数，因为一条数据需要hash计算5次 且5次的结果要不一样
     */
    private static int[] primeNums = new int[]{17, 19, 29, 31, 37};


    //private static final Logger LOG = LoggerFactory.getLogger(TrackLogService.class);
    private static final Logger LOG = SubLoggerUtils.Logger(SubLoggerUtils.LogFileName.BAITIAO_USER);
//    private static final AtomicLong INT = new AtomicLong(0);

    private static final int MAXLEN = 150;

    @Autowired
    private JedisPool jedisPool;//目前根据场景来说是单机模式使用

    public void process(JSONArray dataArray) {
        Jedis jedis = null;
        Jedis jedis1=null;
        try {
            jedis = jedisPool.getResource();
            //jedis1 = jedisPool.getResource();
            //将上报数据写入到redis中
            Jedis finalJedis = jedis;
           // Jedis finalJedis2 = jedis1;
            //将记录拆分后写入redis的队列中
            dataArray.forEach(data -> {
                JSONObject o = (JSONObject) data;
                if(o.get("uu")!=null) {
                    if ("page".equals(o.get("tv")) && "show".equals(o.get("tl"))) {
                        if (!hasKey(USER_ID_BIT_SET,o.getString("uu"), finalJedis)) {
                            o.put("isfirstday", 1);//标明是新用户
                            addKey(USER_ID_BIT_SET,o.getString("uu"), finalJedis);
                        }
                    }
                }
                HashMap<String, String> r_map = new HashMap<String, String>();
                r_map.put("r_data", JSONObject.toJSONString(o, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.QuoteFieldNames));
                finalJedis.xadd(SingleStreamReceiver.SINGLE_STREAM_MQ_KEY, StreamEntryID.NEW_ENTRY, r_map, MAXLEN, true);//目前采用系统自增ID

         });
        } catch (JedisException e) {
            LOG.error(e.getMessage(), e);
            LOG.error("error record=" + JSONArray.toJSONString(dataArray));
            TxtUtils txtUtils = new TxtUtils();
            dataArray.forEach(data -> {
                JSONObject o = (JSONObject) data;
                HashMap<String, String> t_map = new HashMap<String, String>();
                t_map.put("r_data", JSONObject.toJSONString(o, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.QuoteFieldNames));
                txtUtils.saveToFile(t_map);
                LOG.info("数据写入文件成功");
            });
        } finally {
            returnToPool(jedis);
           // returnToPool(jedis1);
        }
    }


    /**
     * 添加元素到bitSet中
     * 在bitset中设置key和value
     * @param key
     */
    public void addKey(String key,String uu,Jedis jedis) {
        for (int i : primeNums) {
            // 计算hashcode
            int hashcode = hash(uu, i);
            // 计算映射在bitset上的位置
            int bitIndex = hashcode & (length - 1);
           // setBloomFilterKey(USER_ID_BIT_SET, bitIndex, true ,jedis);
            try {
                jedis.setbit(key, bitIndex, true);
            }catch (Exception e){
                LOG.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 判断bitSet中是否有被查询的的key(经过hash处理之后的)
     *
     * @param key
     * @return
     */
    public  boolean hasKey(String key, String uu,Jedis jedis) {
        for (int i : primeNums) {
            // 计算hashcode
            int hashcode = hash(uu, i);
            // 计算映射在bitset上的位置
            int bitIndex = hashcode & (length - 1);
            // 只要有一个位置对应不上，则返回false
            if (! getBloomFilterValue(key, bitIndex ,jedis)) {
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
     * @param bitIndex bitset的索引值
     * @return
     */
    public  boolean getBloomFilterValue(String key,int bitIndex,Jedis jedis ) {
        boolean flag = false;
        try {
            flag = jedis.getbit(key, bitIndex);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
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






    public JsonFeedback readFileToRedis(String day, String hour) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //将上报数据写入到redis中
            Jedis finalJedis = jedis;
            String path = System.getProperty("user.dir") + "/" + day + "/" + hour + ".txt";
            FileReader fr = new FileReader(path);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            // 按行读取字符串
            while ((str = bf.readLine()) != null) {
                // 2 切割
                String[] fileds = str.split("--");
                HashMap<String, String> r_map = new HashMap<String, String>();
                r_map.put(fileds[0], fileds[1]);
                String s = finalJedis.xadd(SingleStreamReceiver.TEST_STREAM_KEY, StreamEntryID.NEW_ENTRY, r_map, MAXLEN, true).toString();//目前采用系统自增ID
                LOG.info("插入后的返回值" + s);
                System.out.println("插入后的返回值" + s);
            }
            bf.close();
            fr.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new JsonFeedback(ResponseCode.FILE_NOTFOUND_ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonFeedback(ResponseCode.IO_EXCEPTION);
        } catch (JedisException e) {
            e.printStackTrace();
            return new JsonFeedback(ResponseCode.JEDIS_EXCEPTION);
        } finally {
            returnToPool(jedis);
        }
        return new JsonFeedback(ResponseCode.OK);
    }

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }


}
