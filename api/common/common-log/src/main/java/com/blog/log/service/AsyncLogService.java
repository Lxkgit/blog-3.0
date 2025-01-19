package com.blog.log.service;

import com.blog.core.entity.file.log.SysOperLog;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 异步调用日志服务
 *
 * @author kangxu
 */
@Service
public class AsyncLogService {

    /**
     * 保存系统日志记录
     */
    @Async
    public void saveSysLog(SysOperLog sysOperLog) throws Exception {

    }
}
