package com.blog.timer.config;

import com.blog.timer.manager.DefaultTimerManager;
import com.blog.timer.manager.TimerManager;
import com.blog.timer.registry.MemoryTimerTaskRegistry;
import com.blog.timer.registry.TimerTaskRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Configuration
public class TimerConfig {

    @Bean
    public TimerTaskRegistry timerTaskRegistry() {
        return new MemoryTimerTaskRegistry();
    }

    /**
     *
     * @param registry Bean 方法参数自动注入 TimerTaskRegistry
     * @return
     */
    @Bean
    public TimerManager timerManager(TimerTaskRegistry registry) {
        // 获取CPU核心数作为线程池核心线程数量
        return new DefaultTimerManager(Runtime.getRuntime().availableProcessors(), registry);
    }

}
