package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringPublisherTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage2SimpleQueue() {
        // 此处的queue必须先存在才可以进行发送消息
        rabbitTemplate.convertAndSend("simple.queue", "hello spring amqp!");
    }

    @Test
    public void testSendMessage2WorkQueue() throws InterruptedException {
        // 此处的queue必须先存在才可以进行发送消息
        String message = "hello message___";
        for (int i = 1; i <= 50; i++) {
            rabbitTemplate.convertAndSend("simple.queue", message + i);
            Thread.sleep(20);
        }
    }

    @Test
    public void testSendMessage2FanoutExchange() {
        String exchange = "rabbit.fanout";
        String message = "hello everyone!";
        // RabbitTemplate.DEFAULT_ROUTING_KEY=""
        // routingKey可以为null
        // exchange 可以为null
        rabbitTemplate.convertAndSend(exchange, null, message);
    }

    @Test
    public void testSendMessage2DirectExchange() {
        String exchange = "rabbit.direct";
        String message = "hello blue!";
        // RabbitTemplate.DEFAULT_ROUTING_KEY=""
        // routingKey可以为null
        // exchange 可以为null
        rabbitTemplate.convertAndSend(exchange, "blue", message);
        String message2 = "hello yellow!";
        rabbitTemplate.convertAndSend(exchange, "yellow", message2);
        String message3 = "hello red!";
        rabbitTemplate.convertAndSend(exchange, "red", message3);
    }

    @Test
    public void testSendMessage2TopicExchange() {
        String exchange = "rabbit.topic";
        String message = "呵呵呵呵呵呵恩和!";
        // RabbitTemplate.DEFAULT_ROUTING_KEY=""
        // routingKey可以为null
        // exchange 可以为null
        rabbitTemplate.convertAndSend(exchange, "china.news", message);
        String message2 = "今天天气不错!";
        rabbitTemplate.convertAndSend(exchange, "china.weather", message2);
    }

    @Test
    public void testSendMessage2ObjectQueue() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "柳岩");
        map.put("age", "21");
        rabbitTemplate.convertAndSend("object.queue", map);
    }

}
