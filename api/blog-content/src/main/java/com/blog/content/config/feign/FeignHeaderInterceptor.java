package com.blog.content.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Description openfeign配置
 * @Author lxk
 * @CreateTime 2025-04-01
 */

@Component
public class FeignHeaderInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {

        // 从当前请求上下文中获取原始请求头
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {

            // 请求头添加鉴权信息
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (StringUtils.isNotEmpty(authHeader)) {
                template.header("Authorization", authHeader);
            }

            // 请求头添加认证id
            String rzId = attributes.getRequest().getHeader("rzId");
            if (StringUtils.isNotEmpty(rzId)) {
                template.header("rzId", rzId);
            }
        }
    }
}
