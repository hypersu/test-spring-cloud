package com.example.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.order.client.AccountClient;
import com.example.order.client.StorageClient;
import com.example.order.entity.Order;
import com.example.order.mapper.OrderMapper;
import com.example.order.service.IOrderService;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    private final AccountClient accountClient;
    private final StorageClient storageClient;

    public OrderServiceImpl(AccountClient accountClient, StorageClient storageClient) {
        this.accountClient = accountClient;
        this.storageClient = storageClient;
    }

    @Override
    public Long create(Order order) {
        // 创建订单
        getBaseMapper().insert(order);
        try {
            // 扣用户余额
            accountClient.deduct(order.getUserId(), order.getMoney());
            // 扣库存
            storageClient.deduct(order.getCommodityCode(), order.getCount());

        } catch (FeignException e) {
            log.error("下单失败，原因:{}", e.contentUTF8(), e);
            throw new RuntimeException(e.contentUTF8(), e);
        }
        return order.getId();
    }
}
