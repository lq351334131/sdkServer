package com.etocrm.sdk.sdkserver.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.etocrm.sdk.sdkserver.base.JsonFeedback;
import com.etocrm.sdk.sdkserver.base.ResponseCode;
import com.etocrm.sdk.sdkserver.listener.SingleStreamReceiver;
import com.etocrm.sdk.sdkserver.utils.SubLoggerUtils;
import com.etocrm.sdk.sdkserver.utils.TxtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 *
 */
@Service
public class TrackLogService {

    private static final Logger LOG = LoggerFactory.getLogger(TrackLogService.class);
//    private static final Logger LOG = SubLoggerUtils.Logger(SubLoggerUtils.LogFileName.BAITIAO_USER);
//    private static final AtomicLong INT = new AtomicLong(0);

    private static final int MAXLEN = 150;

    @Autowired
    private JedisPool jedisPool;//目前根据场景来说是单机模式使用

    public void process(JSONArray dataArray) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //将上报数据写入到redis中
            Jedis finalJedis = jedis;
            //将记录拆分后写入redis的队列中
            dataArray.forEach(data -> {
                JSONObject o = (JSONObject) data;
                HashMap<String, String> r_map = new HashMap<String, String>();
                r_map.put("r_data", JSONObject.toJSONString(o, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.QuoteFieldNames));
                finalJedis.xadd(SingleStreamReceiver.SINGLE_STREAM_MQ_KEY, StreamEntryID.NEW_ENTRY, r_map, MAXLEN, true);//目前采用系统自增ID
            });
        } catch (JedisException e) {
            LOG.error(e.getMessage(), e);
            LOG.error("error record=" + JSONArray.toJSONString(dataArray));
            TxtUtils txtUtils =new TxtUtils();
            dataArray.forEach(data -> {
                JSONObject o = (JSONObject) data;
                HashMap<String, String> t_map = new HashMap<String, String>();
                t_map.put("r_data", JSONObject.toJSONString(o, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.QuoteFieldNames));
                txtUtils.saveToFile(t_map);
                LOG.info("数据写入文件成功");
            });

        } finally {
            returnToPool(jedis);
        }
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
        }catch (JedisException e){
            e.printStackTrace();
            return new JsonFeedback(ResponseCode.JEDIS_EXCEPTION);
        }finally {
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
