package com.example.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.order.entity.Order;

public interface IOrderService extends IService<Order> {

    Long create(Order order);
}