package com.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class PublisherTest {
    @Test
    public void testSendMessage() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.119.128");
        factory.setPort(5672);
        factory.setUsername("rabbit");
        factory.setPassword("rabbit");
        factory.setVirtualHost("/");

        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();

        String queue = "simple.queue";

        channel.queueDeclare(queue, false, false, false, null);

        String message = "hello word!";
        channel.basicPublish("", queue, null, message.getBytes());

        channel.close();
        connection.close();
    }
}
