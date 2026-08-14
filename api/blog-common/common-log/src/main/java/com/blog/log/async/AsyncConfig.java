package com.blog.log.async;

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
    @Bean(name = "logThread")
    public ThreadPoolTaskExecutor baseImportThread() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(2);
        // 最大线程数 不要超过 CPU 核 * 2
        executor.setMaxPoolSize(4);
        // 队列容量 避免一次性堆太多任务
        executor.setQueueCapacity(200);
        // 线程空闲时间
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("logThread");
        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
