package com.etocrm.sdk.server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.etocrm.sdk.server.base.JsonFeedback;
import com.etocrm.sdk.server.base.ResponseCode;
import com.etocrm.sdk.server.service.TrackLogService;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author qi.li
 * @time 2020/08/07
 */
@RestController
public class TrackLogController {

    @Autowired
    private TrackLogService tracklogService;


    private static final Logger LOG = LoggerFactory.getLogger(TrackLogController.class);

    /**
     * jsonvalue:
     * <p>
     * {
     * data:[{上报数据1},{上报数据2},{上报数据3},{上报数据4}],
     * len: 4
     * }
     * data是批量发送数据的list
     * len，表示数据记录数
     *
     * @param req
     * @param jsonvalue
     */
    @RequestMapping(value = "/api/track/miniapplog", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    public JsonFeedback receivelog(HttpServletRequest req, @RequestBody String jsonvalue) {
       // String strRealIp = getIpAddress(req);
        //LOG.info("request real IP=" + strRealIp);
        Map mapTypes = JSON.parseObject(jsonvalue, Map.class);
        JSONArray datas = (JSONArray) mapTypes.get("data");
        //String finalStrRealIp = strRealIp;
        datas.forEach(data -> {
            JSONObject o = (JSONObject) data;
           // o.put("ReqIP", finalStrRealIp);
        });

        LOG.info("req=" + jsonvalue);
        //LOG.error("req=" + jsonvalue);
        //将上报数据写入到redis临时缓冲队列中
        tracklogService.process(datas);
        // 返回结果
        LOG.info("req is end");
        return new JsonFeedback(ResponseCode.OK);
    }

    @RequestMapping (value = "/api/readToRedis",method = RequestMethod.POST)
    public JsonFeedback readFileToRedis( @RequestParam String day,@RequestParam String hour) {
        if(day==null || hour==null){
            return new JsonFeedback(ResponseCode.MissParam);
        }else
        {
            return tracklogService.readFileToRedis(day,hour);
        }
    }

    //获取IP信息
    public String getIpAddress(HttpServletRequest request) {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");

        if (!Strings.isNullOrEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if (index != -1) {
                return XFor.substring(0, index);
            } else {
                return XFor;
            }
        }
        XFor = Xip;
        if (!Strings.isNullOrEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)) {
            return XFor;
        }
        if (Strings.nullToEmpty(XFor).trim().isEmpty() || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("Proxy-Client-IP");
        }
        if (Strings.nullToEmpty(XFor).trim().isEmpty() || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (Strings.nullToEmpty(XFor).trim().isEmpty() || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (Strings.nullToEmpty(XFor).trim().isEmpty() || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (Strings.nullToEmpty(XFor).trim().isEmpty() || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getRemoteAddr();
        }
        return XFor;
    }
}
