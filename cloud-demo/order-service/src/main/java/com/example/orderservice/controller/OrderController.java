package com.example.orderservice.controller;

import com.example.orderservice.pojo.Order;
import com.example.orderservice.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @GetMapping("{id}")
    public Order getById(@PathVariable("id") String id) {
        return orderService.getByIdExternal(id);
    }

}
