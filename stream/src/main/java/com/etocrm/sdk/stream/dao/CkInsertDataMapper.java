package com.etocrm.sdk.stream.dao;

import com.etocrm.sdk.stream.entity.Salary;

import java.util.List;

/**
 * @Author qi.li
 * @create 2020/11/18 17:55
 */
public interface CkInsertDataMapper {

    Integer batchInsert(List<Salary> list);

}
