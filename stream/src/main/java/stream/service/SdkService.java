package stream.service;


import com.eto.sdk.stream.dao.CkInsertDataMapper;
import com.eto.sdk.stream.entity.SdkNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author qi.li
 * @create 2020/11/9 17:30
 */
@Service
@Slf4j
public class SdkService {

    @Autowired
    private CkInsertDataMapper ckInsertDataMapper;

//    //单条插入
//    public void insertSingle(String value){
//        Sdk sdk= JSONObject.parseObject(value, Sdk.class);
//        try {
//            sdkDao.insertSingle(sdk);
//        }catch (Exception e){
//            log.error(e.getMessage(),e);
//        }
//
//    }

    //批量插入
    public void insertBatch(List<SdkNew> sdkList){
        try {
            ckInsertDataMapper.batchInsert(sdkList);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            String sdk=listToString(sdkList);
            log.error("Sdk List insert"+sdk);
        }

    };

    public String listToString(List<SdkNew> sdkList) {
        if (null == sdkList && sdkList.size() <= 0) {
            return "";
        } else {

            StringBuilder sb = new StringBuilder();
            String resultString = "";
            for (int i = 0; i < sdkList.size(); i++) {
                if (i < sdkList.size() - 1) {
                    sb.append(sdkList.get(i));
                    sb.append("/n");
                } else {
                    sb.append(sdkList.get(i));
                }
            }

            resultString = sb.toString();
            return resultString;
        }
    }

}
