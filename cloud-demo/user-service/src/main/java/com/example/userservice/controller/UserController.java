package com.example.userservice.controller;

import com.example.userservice.pojo.User;
import com.example.userservice.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;

    @GetMapping("{id}")
    public User getById(@PathVariable("id") String id) {
        return userService.getById(id);
    }
}
