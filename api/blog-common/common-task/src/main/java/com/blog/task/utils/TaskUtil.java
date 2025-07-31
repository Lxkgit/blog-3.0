package com.blog.task.utils;

import com.blog.redis.service.RedisService;
import com.blog.task.domain.TaskEntity;
import jakarta.annotation.Resource;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-31
 */

public class TaskUtil {

    @Resource
    private RedisService redisService;

    public void createTask(TaskEntity taskEntity) {
        redisService.

    }


}
