package com.etocrm.sdk.sdkserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

//@Configuration
public class RedisConfig {
    //    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        // 序列化方式全部为String
//        redisTemplate.setKeySerializer(RedisSerializer.string());
//        redisTemplate.setValueSerializer(RedisSerializer.string());
//        redisTemplate.setHashKeySerializer(RedisSerializer.string());
//        // hash value序列化方式为JSON
//        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
//        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
//        return redisTemplate;
//    }
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory(JedisPoolConfig jedisPool,
//                                                         RedisStandaloneConfiguration jedisConfig) {
//        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(jedisConfig);
//        connectionFactory.setPoolConfig(jedisPool);
//        return connectionFactory;
//    }
//
//
//    @Bean
//    public JedisPoolConfig jedisPool() {
//        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        jedisPoolConfig.setMaxIdle(maxIdle);
//        jedisPoolConfig.setMaxWaitMillis(maxWait);
//        jedisPoolConfig.setMaxTotal(maxActive);
//        jedisPoolConfig.setMinIdle(minIdle);
//        return jedisPoolConfig;
//    }
//
//    @Bean
//    public RedisStandaloneConfiguration jedisConfig() {
//        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
//        config.setHostName(host);
//        config.setPort(port);
//        config.setDatabase(database);
//        config.setPassword(RedisPassword.of(password));
//        return config;
//    }
}

