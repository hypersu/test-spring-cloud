package com.example.service.impl;

import com.example.mapper.HotelMapper;
import com.example.pojo.Hotel;
import com.example.service.IHotelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {
}
