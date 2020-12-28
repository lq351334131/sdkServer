package com.etocrm.sdk.server.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Collections;

/**
 * 按照消费组的模式对Stream模式进行消费处理
 *
 */
//@Component
public class GroupStreamReceiver implements ApplicationListener<ApplicationStartedEvent> {

    public static final String SINGLE_STREAM_MQ_KEY111 = "STREAM_QUEUE";
    private static final Logger LOG = LoggerFactory.getLogger(GroupStreamReceiver.class);

//    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        // 创建配置对象
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> streamMessageListenerContainerOptions = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                // 一次性最多拉取多少条消息
                .batchSize(10)

//                // 超时时间，设置为0，表示不超时, 默认 2秒
//                .pollTimeout(Duration.ZERO)

//                // 序列化器, 默认 StringRedisSerializer.UTF_8
//                .serializer(StringRedisSerializer.UTF_8)

//                // 消费消息的线程池, 默认 SimpleAsyncTaskExecutor
//                .executor(Executors.newFixedThreadPool(10))

//                // 消息消费异常的handler, 默认写出到 log
//                .errorHandler(Throwable::printStackTrace)

//                // 修改 每条消息的 value 类型
//                .targetType(Object.class)

//                // 修改 消息键值对类型, 默认 ObjectHashMapper
//                .objectMapper(new Jackson2HashMapper(true))

                .build();

        // 根据配置对象创建监听容器
        return StreamMessageListenerContainer
                .create(redisConnectionFactory, streamMessageListenerContainerOptions);
    }


    //@Bean
    public StreamListener<String, MapRecord<String, String, String>> streamListener(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer,
            RedisTemplate<String, String> redisTemplate, Environment environment) throws UnknownHostException {

        String streamKey = SINGLE_STREAM_MQ_KEY111;
        String groupId = environment.getRequiredProperty("spring.application.name");
        String consumerName = Inet4Address.getLocalHost().getHostName() + ":" + environment.getProperty("server.port");
        LOG.info("streamKey = " + streamKey + ", groupId = " + groupId + ", consumerName = " + consumerName);

        // 判断 stream 是否初始化，未初始化则进行初始化
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(streamKey))) {
            // 往 stream 发送消息，会自动创建 stream
            RecordId recordId = redisTemplate.opsForStream().add(streamKey, Collections.singletonMap("_up", "up"));

            // 创建 消费者组
            redisTemplate.opsForStream().createGroup(streamKey, groupId);

            // 删除创建
            redisTemplate.opsForStream().delete(streamKey, recordId);
        }

        // 监听器
        StreamListener<String, MapRecord<String, String, String>> listener = message -> message.forEach(System.out::println);

        // 使用监听容器监听消息，并且自动应答
        streamMessageListenerContainer.receiveAutoAck(
                Consumer.from(groupId, consumerName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                listener);
        return listener;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        // 启动redis stream 监听
        applicationStartedEvent.getApplicationContext()
                .getBeanProvider(StreamMessageListenerContainer.class)
                .ifAvailable(container -> container.start());
    }
}
