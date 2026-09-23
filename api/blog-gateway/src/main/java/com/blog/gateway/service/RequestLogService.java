package com.blog.gateway.service;


import com.blog.core.domain.gateway.RequestLog;

public interface RequestLogService {

    int saveRequestLog(RequestLog requestLog);
}
