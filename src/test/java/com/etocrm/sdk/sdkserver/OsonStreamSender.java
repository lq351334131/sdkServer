package com.etocrm.sdk.sdkserver;


import com.etocrm.sdk.sdkserver.listener.GroupStreamReceiver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.BoundStreamOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
//@SpringBootApplication
public class OsonStreamSender {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void sendMessage() {
        BoundStreamOperations<String, String, String> operations = redisTemplate.boundStreamOps(GroupStreamReceiver.SINGLE_STREAM_MQ_KEY111);
        for (int i = 0; i < 10; i++) {
            RecordId recordId = operations.add(Collections.singletonMap("index", String.valueOf(i)));
            System.out.printf("send message %d , message id %s\n", i, recordId);
        }
    }
}
