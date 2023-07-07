package com.example.service;

import com.example.pojo.Hotel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.pojo.PageResult;
import com.example.pojo.RequestParams;

public interface IHotelService extends IService<Hotel> {
   PageResult search(RequestParams requestParams);
}
