package com.example.feignapi.clients.fallback;

import com.example.feignapi.clients.UserClient;
import com.example.feignapi.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public User findById(Long id) {
                log.info("这是一个空的User");
                return new User();
            }
        };
    }
}
