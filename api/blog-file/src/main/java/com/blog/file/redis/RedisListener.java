package com.blog.file.redis;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * @Description redis 数据监听
 * @Author lxk
 * @CreateTime 2025-08-25
 */

@Component
public class RedisListener implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(RedisListener.class);

    @Resource
    private Executor systemTaskThread;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        systemTaskThread.execute(this::redisListenerThread);
    }


    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    private void redisListenerThread() {
        while (true) {
            try {

                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("redis 数据处理异常：{}", e.getMessage(), e);
            }
        }
    }
}
