package com.example.config;

import com.example.constants.HotelMqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HotelMqConfiguration {
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(HotelMqConst.EXCHANGE_NAME);
    }

    @Bean
    public Queue insertQueue() {
        return new Queue(HotelMqConst.INSERT_QUEUE_NAME);
    }

    @Bean
    public Queue deleteQueue() {
        return new Queue(HotelMqConst.DELETE_QUEUE_NAME);
    }

    @Bean
    public Binding insertBinding() {
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(HotelMqConst.INSERT_KEY);
    }

    @Bean
    public Binding deleteBinding() {
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(HotelMqConst.DELETE_KEY);
    }
}
