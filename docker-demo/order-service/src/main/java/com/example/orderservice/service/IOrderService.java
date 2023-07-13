package com.example.orderservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.orderservice.pojo.Order;

import java.io.Serializable;

public interface IOrderService extends IService<Order> {
    Order getByIdExternal(Serializable id);
}
