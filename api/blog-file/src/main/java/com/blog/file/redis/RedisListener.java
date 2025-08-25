package com.blog.file.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @Description redis 数据监听
 * @Author lxk
 * @CreateTime 2025-08-25
 */

@Component
public class RedisListener implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(RedisListener.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        redisListenerThread();
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
