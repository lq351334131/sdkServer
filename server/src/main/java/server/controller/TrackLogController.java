package server.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.etocrm.sdk.server.base.JsonFeedback;
import com.etocrm.sdk.server.base.ResponseCode;
import com.etocrm.sdk.server.service.TrackLogService;
import com.etocrm.sdk.server.utils.SubLoggerUtils;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    private static final Logger log = LoggerFactory.getLogger(TrackLogController.class);

    private static final Logger LOG = SubLoggerUtils.Logger(SubLoggerUtils.LogFileName.BAITIAO_USER);




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
    public Object receivelog(HttpServletRequest req, @RequestBody String jsonvalue) {
        String strRealIp = getIpAddress(req);
        Map map = JSON.parseObject(jsonvalue, Map.class);
        JSONArray datas = (JSONArray) map.get("data");
        int rt;
        if(map.containsKey("rt") && map.get("rt")!=null ){
            rt= (int) map.get("rt");
        }else {
            rt=-1;
        }


        String finalStrRealIp = strRealIp;
        for (int i = 0; i < datas.size(); i++) {
            JSONObject mapTypes = datas.getJSONObject(i);
            mapTypes.put("ReqIP", finalStrRealIp);
            mapTypes.put("rt", rt);
            if (mapTypes.get("tv") == null ||
                    mapTypes.get("b") == null ||
                    mapTypes.get("c") == null ||
                    mapTypes.get("k") == null ||
                    mapTypes.get("t") == null ||
                    mapTypes.get("uu") == null ) {
                //break;
                return new JsonFeedback(ResponseCode.MissParam);
            }
            LOG.info(JSONObject.toJSONString(mapTypes, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.QuoteFieldNames));
        }
        if (datas != null) {
            return tracklogService.sendKafka(datas);
        }else{
            return new JsonFeedback(ResponseCode.Fail);
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
        if(XFor !=null){
            return XFor;
        }else {
            return "0.0.0.0.1";
        }

    }
}
