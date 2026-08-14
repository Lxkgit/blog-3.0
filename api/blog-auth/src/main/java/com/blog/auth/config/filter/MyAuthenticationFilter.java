package com.blog.auth.config.filter;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.utils.JwtUtil;
import com.blog.core.utils.SecurityUtil;
import com.blog.redis.constant.AuthRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * 认证过滤器 校验通过 就不需要再登陆
 * @author 27992
 */

@Component
public class MyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(MyAuthenticationFilter.class);

    @Resource
    private RedisService redisService;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String token = request.getHeader("Authorization");

            if (StringUtils.isNotEmpty(token) && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                // 检查 Token 是否已经退出登录
                String logoutKey = AuthRedisConstant.LOGOUT_TOKEN + ":" + jwtToken;
                if (redisService.getString(logoutKey) != null) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                JSONObject jwt = JwtUtil.decodeJwt(jwtToken);
                SecurityUtil.setLoginUser(jwt);
            }

        } catch (Exception e) {
            logger.error("JWT认证异常: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
