package com.blog.gateway.service.impl;

import com.blog.core.domain.gateway.RequestLog;
import com.blog.gateway.mapper.RequestLogMapper;
import com.blog.gateway.service.RequestLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Author: lxk
 * @date 2022/7/20 17:12
 * @description: 接口请求日志服务
 */

@Service
public class RequestLogServiceImpl implements RequestLogService {

    @Resource
    private RequestLogMapper requestLogMapper;

    @Override
    public int saveRequestLog(RequestLog requestLog) {
        return requestLogMapper.insert(requestLog);

    }

}
