package com.blog.auth.config.filter;

import com.blog.auth.constant.RedisConstant;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 认证过滤器 校验通过 就不需要再登陆
 */
@Slf4j
@Component
public class MyAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private RedisTemplate redisTemplate;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //从请求头获取认证id
        String rzId = request.getHeader("rzId");
        if (StringUtils.isEmpty(rzId)) {
            //如果header头是空的 那么在从参数获取认证id
            rzId = request.getParameter("rzId");
        }
        if (StringUtils.isNotEmpty(rzId)) {
            //去redis中获取上下文
            String key = RedisConstant.RZ_ID + ":" + rzId;
            // 根据缓存 获取认证信息
            Object o = redisTemplate.opsForValue().get(key);
            if (o == null) {
                //如果缓存没有 那么放行
                filterChain.doFilter(request, response);
                return;
            }
            SecurityContext securityContext = (SecurityContext) o;
            //把上下文信息放入持有人手中 这样别的请求在进来 就有认证的权限了 就不需要再登陆了
            SecurityContextHolder.setContext(securityContext);
        }
        //放行
        filterChain.doFilter(request, response);
    }


}
