package com.example.controller;

import com.example.pojo.PageResult;
import com.example.pojo.RequestParams;
import com.example.service.IHotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("suggestion")
    public List<String> suggestion(@RequestParam("prefix") String prefix) {
        return hotelService.suggestion(prefix);
    }
}
