package com.example.userservice.controller;

import com.example.userservice.config.PatternProperties;
import com.example.userservice.pojo.User;
import com.example.userservice.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/user")
//@RefreshScope
public class UserController {
    @Autowired
    private IUserService userService;
    //@Value("${pattern.dateformat}")
    //private String dateformat;
    @Autowired
    private PatternProperties properties;

    @GetMapping("prop")
    public PatternProperties properties() {
        return properties;
    }

    @GetMapping("now")
    public String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(properties.getDateformat()));
    }

    /*@GetMapping("{id}")
    public User getById(@PathVariable("id") String id, @RequestHeader("Trust") String trust) {
        System.out.println("Trust :" + trust);
        return userService.getById(id);
    }*/

    @GetMapping("{id}")
    public User getById(@PathVariable("id") Long id) throws InterruptedException {
        if (id == 1) {
            Thread.sleep(60);
        } else if (id == 2) {
            throw new RuntimeException("故意出错，触发熔断规则。");
        }
        return userService.getById(id);
    }
}
