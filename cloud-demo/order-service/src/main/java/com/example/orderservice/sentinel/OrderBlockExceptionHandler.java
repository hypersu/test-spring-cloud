package com.example.orderservice.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderBlockExceptionHandler implements BlockExceptionHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse,
                       BlockException e) throws Exception {
        int status = HttpStatus.TOO_MANY_REQUESTS.value();
        String message = "未知异常";
        if (e instanceof FlowException) {
            message = "请求被限流了";
        } else if (e instanceof ParamFlowException) {
            message = "请求被热点参数限流了";
        } else if (e instanceof DegradeException) {
            message = "请求被降级了";
        } else if (e instanceof AuthorityException) {
            message = "请求没有权限";
            status = HttpStatus.UNAUTHORIZED.value();
        }
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.setStatus(status);
        httpServletResponse.getWriter().println("{\"message\":\""+message+"\",\"status\":"+status+"}");
    }
}
