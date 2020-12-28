package com.etocrm.sdk.stream.base;

import com.etocrm.sdk.stream.entity.Sdk;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Author qi.li
 * @create 2020/11/11 17:41
 */
public class MockData {

    public static List<LinkedHashMap<String, Object>> mockData(LinkedHashMap<String, String> paramType){

        List<LinkedHashMap<String,Object>> result = Lists.newArrayList();
        for (int i = 0; i < 5000; i++) {
            LinkedHashMap<String, Object> stringObjectMap = mockOneData(paramType);
            result.add(stringObjectMap);
        }
        return result;
    }

    private static LinkedHashMap<String,Object> mockOneData(LinkedHashMap<String, String> paramType){
        LinkedHashMap<String,Object> data = Maps.newLinkedHashMap();
        for (String name : paramType.keySet()){
            String ckType =  paramType.get(name);
            CkTypeEnum anEnum = CkTypeEnum.getEnum(ckType);
            Object o = null;
            if(anEnum != null){
                o = anEnum.mockData();
            }
            data.put(name,o);
        }
        return data;
    }


    private static LinkedHashMap<String,Object> mockOneData2(LinkedHashMap<String, String> paramType, Sdk sdk){
        LinkedHashMap<String,Object> data = Maps.newLinkedHashMap();
        for (String name : paramType.keySet()){
            String ckType =  paramType.get(name);
            CkTypeEnum anEnum = CkTypeEnum.getEnum(ckType);
            Object o = null;
            if(anEnum != null){
                o = anEnum.mockData();
            }
            data.put(name,o);
        }
        return data;
    }


}
