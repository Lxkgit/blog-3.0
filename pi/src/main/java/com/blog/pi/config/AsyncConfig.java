package com.blog.pi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-28
 */

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 基础线程池
     * 使用：
     *     @Resource
     *     private Executor baseThread;
     * @return
     */
    @Bean(name = "baseThread")
    public ThreadPoolTaskExecutor baseImportThread() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(10);
        // 队列容量
        executor.setQueueCapacity(500);
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("BaseImportThread");
        // 拒绝策略 由调用者所在的线程来执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
