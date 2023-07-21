package com.example.feignapi.config;

import com.example.feignapi.clients.fallback.UserClientFallbackFactory;
import feign.Logger;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfiguration {
    @Bean
    public Logger.Level logger() {
        return Logger.Level.FULL;
    }

    @Bean
    public UserClientFallbackFactory userClientFallbackFactory() {
        return new UserClientFallbackFactory();
    }
}
