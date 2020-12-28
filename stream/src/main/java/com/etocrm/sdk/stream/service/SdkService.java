package com.etocrm.sdk.stream.service;

import com.alibaba.fastjson.JSONObject;
import com.etocrm.sdk.stream.dao.CkInsertDataMapper;
import com.etocrm.sdk.stream.entity.Salary;
import com.etocrm.sdk.stream.entity.Sdk;
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
    public void insertBatch(List<Salary> sdkList){
        try {
            ckInsertDataMapper.batchInsert(sdkList);
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }

    };
}
