package com.blog.task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-30
 */

@EnableAsync
@Configuration
public class TaskThread {

    /**
     * 基础线程池
     * 使用：
     *     @Resource
     *     private Executor baseThread;
     * @return
     */
    @Bean(name = "baseTaskThread")
    public ThreadPoolTaskExecutor baseImportThread() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(2);
        // 最大线程数
        executor.setMaxPoolSize(4);
        // 队列容量
        executor.setQueueCapacity(500);
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("TaskThread");
        // 拒绝策略 由调用者所在的线程来执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
