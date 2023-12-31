package com.example.orderservice;

import com.example.feignapi.clients.UserClient;
import com.example.feignapi.config.DefaultFeignConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableFeignClients(clients = UserClient.class, defaultConfiguration = DefaultFeignConfiguration.class)
// 配置随机策略
//@LoadBalancerClient(name = "userservice", configuration = LoadBalancerConfiguration.class)
// 配置nacos策略
@LoadBalancerClient(name = "userservice", configuration = NacosLoadBalancerConfiguration.class)
@SpringBootApplication
public class OrderServiceApplication {
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }


    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
