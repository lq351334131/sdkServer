package com.etocrm.sdk.stream.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
/**
 * @Author qi.li
 * @create 2020/11/18 18:45
 */
@Data
@Component
//@ConfigurationProperties(prefix = "spring.datasource.click")
public class JdbcParamConfig {

    @Value("${spring.datasource.click.driverClassName}")
    private String driverClassName ;
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @Value("${spring.datasource.click.url}")
    private String url ;
    public void setUrl(String url) {
        this.url = url;
    }

    @Value("${spring.datasource.click.username}")
    private String username ;
    public void setUsername(String username) { this.username = username; }
 
    @Value("${spring.datasource.click.password}")
    private String password ;

    public void setPassword(String password) { this.password = password; }

    @Value("${spring.datasource.click.initialSize}")
    private Integer initialSize ;
    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    @Value("${spring.datasource.click.maxActive}")
    private Integer maxActive ;
    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    @Value("${spring.datasource.click.minIdle}")
    private Integer minIdle ;
    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    @Value("${spring.datasource.click.maxWait}")
    private Integer maxWait ;
    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

}
