package com.blog.timer.config;

import com.blog.timer.manager.DefaultTimerManager;
import com.blog.timer.manager.TimerManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Configuration
public class TimerConfig {


    /**
     *
     * @param
     * @return
     */
    @Bean
    public TimerManager timerManager() {
        // 获取CPU核心数作为线程池核心线程数量
        return new DefaultTimerManager(Runtime.getRuntime().availableProcessors());
    }

}
