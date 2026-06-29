package com.blog.task.listener;

import com.blog.core.domain.file.task.del.entity.TaskLog;
import com.blog.redis.service.RedisService;
import com.blog.task.constant.TaskConstant;
import com.blog.task.mapper.TaskLogMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.Executor;

/**
 * @Description redis 数据监听
 * @Author lxk
 * @CreateTime 2025-08-25
 */

@Component
public class RedisListener implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(RedisListener.class);

    @Value("${task.thread}")
    private Boolean thread;

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


    @SuppressWarnings({"BusyWait"})
    private void redisListenerThread() {
        logger.info("任务日志记录线程启动");
        while (thread) {
            try {
                insertTaskLog();
                Thread.sleep(10 * 1000);
            } catch (Exception e) {
                logger.error("redis 数据处理异常：{}", e.getMessage(), e);
            }
        }
    }

    /**
     * 记录任务日志
     */
    private void insertTaskLog() {
        while (redisService.getListSize(TaskConstant.TASK_LOG) != 0) {
            try {
                Object o = redisService.getListLeftPop(TaskConstant.TASK_LOG);
                if (!ObjectUtils.isEmpty(o)) {
                    TaskLog taskLog = (TaskLog) o;
                    taskLog.setId(null);
                    taskLogMapper.insertTaskLog(taskLog);
                    taskLogMapper.updateTaskLogEndTimeByTaskUUID(taskLog.getTaskUUID());
                }
            } catch (Exception e) {
                logger.error("日志写入失败: {}", e.getMessage(), e);
            }
        }
    }
}
