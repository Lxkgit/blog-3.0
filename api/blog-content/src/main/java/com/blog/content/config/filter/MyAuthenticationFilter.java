package com.blog.content.config.filter;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.utils.JwtUtil;
import com.blog.core.utils.SecurityUtil;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * 认证过滤器 校验通过 就不需要再登陆
 */
@Slf4j
@Component
public class MyAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private RedisService redisService;

    /**
     * 所有请求的过滤器
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
            throws ServletException, IOException {


        try {
            String token = request.getHeader("Authorization");
            if(StringUtils.isNotEmpty(token)) {
                JSONObject jwt = JwtUtil.decodeJwt(token.substring(7));
                SecurityUtil.setLoginUser(jwt);
            }
            //从请求头获取认证id
            String rzId = request.getHeader("rzId");
            if (StringUtils.isNotEmpty(rzId)) {
                SecurityUtil.setRzId(rzId);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        //放行
        filterChain.doFilter(request, response);
    }


}
