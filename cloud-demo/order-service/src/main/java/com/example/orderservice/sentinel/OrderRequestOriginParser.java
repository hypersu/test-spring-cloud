package com.example.orderservice.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.util.StringUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderRequestOriginParser implements RequestOriginParser {
    @Override
    public String parseOrigin(HttpServletRequest httpServletRequest) {
        String origin = httpServletRequest.getHeader("origin");
        if (StringUtil.isBlank(origin)) {
            origin = "black";
        }
        return origin;
    }
}
