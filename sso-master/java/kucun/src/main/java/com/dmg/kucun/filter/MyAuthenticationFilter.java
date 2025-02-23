package com.dmg.kucun.filter;

import com.dmg.kucun.service.RsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 自定义认证过滤器 只登陆1次
 */
@Slf4j
@Component
public class MyAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private RsService rsService;
 

    /**
     * 所有请求的过滤器
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //获取token信息
        String header=request.getHeader("Authorization");
        log.info("header:{}",header);
        //注意Bearer后面还有一个空格
        if(StringUtils.isEmpty(header) || !StringUtils.startsWithIgnoreCase(header,"Bearer ")){
            //如果请求头是空的 或者 前置没有以Bearer 开头 那么进入下一个过滤器链
            filterChain.doFilter(request,response);
            return;
        }
        //把Bearer空格去掉
        String token=header.substring(7);
        //拿着token 去资源服务器校验是否存在
        Authentication authentication = rsService.getAuthentication(token);
        if(authentication!=null){
            //将 Authentication 对象存储在客户端中 这样客户端就有认证信息了 就不会再出现401和403了
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        //放行
        filterChain.doFilter(request,response);
    }


}
