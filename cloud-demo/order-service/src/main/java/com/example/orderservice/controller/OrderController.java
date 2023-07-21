package com.example.orderservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.example.orderservice.pojo.Order;
import com.example.orderservice.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private IOrderService orderService;

    @SentinelResource("hot")
    @GetMapping("{id}")
    public Order getById(@PathVariable("id") Long id) {
        return orderService.getByIdExternal(id);
    }

    @GetMapping("update")
    public String update() {
        return "更新订单成功";
    }

    @GetMapping("query")
    public String query() {
        orderService.queryGoods();
        return "查询订单成功";
    }

    @GetMapping("save")
    public String save() {
        orderService.queryGoods();
        return "保存订单成功";
    }
}
