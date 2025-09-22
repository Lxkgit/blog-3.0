package com.blog.file.redis;

import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.file.mapper.TaskLogMapper;
import com.blog.redis.service.RedisService;
import com.blog.task.constant.TaskConstant;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
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
    private RedisService redisService;

    @Resource
    private Executor systemTaskThread;

    @Resource
    private TaskLogMapper taskLogMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        systemTaskThread.execute(this::redisListenerThread);
    }


    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    private void redisListenerThread() {
        while (true) {
            try {
                insertTaskLog();
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("redis 数据处理异常：{}", e.getMessage(), e);
            }
        }
    }

    /**
     * 记录任务日志
     */
    private void insertTaskLog() {
        List<Object> list = redisService.getList(TaskConstant.TASK_LOG, 0, -1);
        if (CollectionUtils.isNotEmpty(list)) {
            for (Object o : list) {
                TaskLog taskLog = (TaskLog) o;
                taskLogMapper.insert(taskLog);
            }
        }
    }
}
