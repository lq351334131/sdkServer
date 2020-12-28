package com.etocrm.sdk.stream.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.annotation.Resource;
import javax.sql.DataSource;
/**
 * @Author qi.li
 * @create 2020/11/18 18:44
 */
@Configuration
public class DruidConfig {
    @Resource
    private JdbcParamConfig jdbcParamConfig ;

    @Bean
    public DataSource dataSource() {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(jdbcParamConfig.getUrl());
        datasource.setUsername(jdbcParamConfig.getUsername());
        datasource.setPassword(jdbcParamConfig.getPassword());
        datasource.setDriverClassName(jdbcParamConfig.getDriverClassName());
        datasource.setInitialSize(jdbcParamConfig.getInitialSize());
        datasource.setMinIdle(jdbcParamConfig.getMinIdle());
        datasource.setMaxActive(jdbcParamConfig.getMaxActive());
        datasource.setMaxWait(jdbcParamConfig.getMaxWait());
        return datasource;
    }
}
