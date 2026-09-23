package com.blog.gateway.filiter;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.auth.bo.LoginUserBo;
import com.blog.core.domain.gateway.RequestLog;
import com.blog.core.utils.IpUtil;
import com.blog.core.utils.JwtUtil;
import com.blog.core.utils.SecurityUtil;
import com.blog.gateway.service.BlacklistIpService;
import com.blog.gateway.service.RequestLogService;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Date;
import java.util.List;

/**
 * @Author: lxk
 * @date 2022/7/19 16:25
 * @description:
 */

@Component
public class GatewayFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(GatewayFilter.class);

    private static final String BLACKLIST_IP_KEY = "gateway:blacklist:ip";

    @Resource
    private RequestLogService requestLogService;

    @Resource
    private RedisService redisService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String ip = IpUtil.getIpAddress(exchange.getRequest());

        // 黑名单判断：必须同步
        if (redisService.hasSetByValue(BLACKLIST_IP_KEY, ip)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        // JWT 解析
        try {
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (StringUtils.isNotEmpty(token) && token.startsWith("Bearer ")) {
                JSONObject jwt = JwtUtil.decodeJwt(token.substring(7));
                SecurityUtil.setLoginUser(jwt);
            }
        } catch (Exception e) {
            logger.error("用户鉴权信息获取异常:{}", e.getMessage(), e);
        }

        // 构建请求日志
        RequestLog requestLog = new RequestLog();
        requestLog.setCreateTime(new Date());
        requestLog.setMethod(exchange.getRequest().getMethod().name());
        requestLog.setUrlPath(exchange.getRequest().getPath().toString());
        requestLog.setParam(exchange.getRequest().getQueryParams().toString());
        requestLog.setRequestIp(ip);

        LoginUserBo loginUser = SecurityUtil.getLoginUser();
        if (loginUser != null) {
            requestLog.setUserId(loginUser.getId());
        }

        // 异步保存日志
        Mono.fromRunnable(() -> {
            try {
                requestLogService.saveRequestLog(requestLog);
            } catch (Exception e) {
                logger.error("保存请求日志失败", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();

        // 继续请求
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 10;
    }
}