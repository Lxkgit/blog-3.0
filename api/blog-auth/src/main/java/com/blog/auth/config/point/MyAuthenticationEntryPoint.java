package com.blog.auth.config.point;

import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 我的身份验证入口点
 * 没有登陆认证 异常处理器
 *
 * @param
 * @return
 * @throws Exception
 */
@Slf4j
@Component
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        //返回json格式
        response.setContentType("application/json;charset=utf-8");
        //没有登陆 直接访问其他接口 就报401
        response.setStatus(401);
        Result result = ResultFactory.buildFailResult("401", "请先登录");
        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(result);
        //把json数据 写入 返回给前端
        PrintWriter writer = response.getWriter();
        writer.write(s);
        writer.flush();
        writer.close();
    }
}