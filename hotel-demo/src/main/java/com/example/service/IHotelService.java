package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.pojo.Hotel;
import com.example.pojo.PageResult;
import com.example.pojo.RequestParams;

import java.util.List;
import java.util.Map;

public interface IHotelService extends IService<Hotel> {
    PageResult search(RequestParams requestParams);

    Map<String, List<String>> filters(RequestParams requestParams);

    List<String> suggestion(String prefix);
}
