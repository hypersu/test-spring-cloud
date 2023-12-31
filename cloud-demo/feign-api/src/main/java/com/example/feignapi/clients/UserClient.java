package com.example.feignapi.clients;

import com.example.feignapi.clients.fallback.UserClientFallbackFactory;
import com.example.feignapi.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "userservice", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {
    @GetMapping("user/{id}")
    User findById(@PathVariable("id") Long id);
}
