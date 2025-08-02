package com.blog.task.listener;

import com.blog.redis.service.RedisService;
import com.blog.task.config.TaskThread;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-31
 */

@Component
public class TaskListener implements ApplicationRunner {

    @Resource
    private RedisService redisService;

    @Resource
    private Executor baseTaskThread;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("任务定时监控线程启动");

    }
}
