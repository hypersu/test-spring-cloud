package com.example.orderservice.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.feignapi.clients.UserClient;
import com.example.feignapi.pojo.User;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.pojo.Order;
import com.example.orderservice.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    //    @Autowired
//    private RestTemplate restTemplate;
//
//    @Override
//    public Order getByIdExternal(Serializable id) {
//        Order order = getById(id);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        String url = "http://userservice/user/" + order.getUserId();
//        User user = restTemplate.getForObject(url, User.class);
//        order.setUser(user);
//        return order;
//    }
    @Autowired
    private UserClient userClient;

    @Override
    public Order getByIdExternal(Serializable id) {
        Order order = getById(id);
        User user = userClient.findById(order.getUserId());
        order.setUser(user);
        return order;
    }

    @Override
    @SentinelResource("goods")
    public void queryGoods() {
        System.out.println("正在查询商品");
    }
}
