package com.example.listener;

import com.example.constants.HotelMqConst;
import com.example.service.IHotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HotelMqListener {
    @Autowired
    private IHotelService hotelService;

    @RabbitListener(queues = HotelMqConst.INSERT_QUEUE_NAME)
    public void listenerInsert(String id) {
        hotelService.insertById(id);
    }

    @RabbitListener(queues = HotelMqConst.DELETE_QUEUE_NAME)
    public void listenerDelete(String id) {
        hotelService.deleteById(id);
    }
}
