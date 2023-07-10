package com.example.controller;

import com.example.pojo.PageResult;
import com.example.pojo.RequestParams;
import com.example.service.IHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hotel")
public class HotelController {
    @Autowired
    private IHotelService hotelService;

    @PostMapping("list")
    public PageResult search(@RequestBody RequestParams requestParams) {
        return hotelService.search(requestParams);
    }

    @PostMapping("filters")
    public Map<String, List<String>> filters(@RequestBody RequestParams requestParams) {
        return hotelService.filters(requestParams);
    }
}
