package com.etocrm.sdk.sdkserver;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import javax.swing.*;

public class JedisExample {
    @Test
    public void testFirstExample() {
        // 连接redis
        Jedis jedis = new Jedis("10.10.6.33", 6379);
        jedis.auth("test123");
        // Jedis jedis = new Jedis("localhost"); // 默认6379端口

        // string类型
        jedis.set("name", "demo");
        String name = jedis.get("name");

        // list类型
        jedis.lpush("myList", "hello");
        jedis.rpush("myList", "world");
        String lpopVal = jedis.lpop("myList");
        String rpopVal = jedis.rpop("myList");

        // set类型
        jedis.sadd("mySet", "123");
        jedis.sadd("mySet", "456");
        jedis.sadd("mySet", "789");
        jedis.srem("mySet", "789");
        jedis.scard("mySet");

        // zset类型
        jedis.zadd("myZset", 99, "X");
        jedis.zadd("myZset", 90, "Y");
        jedis.zadd("myZset", 97, "Z");
        Double zscore = jedis.zscore("myZset", "Z");

        // 其他
        jedis.incr("intKey");
        jedis.incrBy("intKey", 5);
        jedis.del("intKey");

        // 触发持久化
        // jedis.save();
        // jedis.bgsave()

        // 关闭连接
        jedis.close();
    }

}
