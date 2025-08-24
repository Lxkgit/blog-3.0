package com.blog.task.service;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.file.task.entity.TaskInfo;
import com.blog.redis.service.RedisService;
import com.blog.task.constant.TaskConstant;
import com.blog.task.domain.TaskEntity;
import com.blog.task.utils.CronUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;


/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-31
 */

@Service
public class CreateTaskService {

    private static final Logger logger = LoggerFactory.getLogger(CreateTaskService.class);

    @Resource
    private RedisService redisService;

    public void createTask(TaskEntity taskEntity) {
        long nextTime = 0L;
        if (taskEntity.getIndexCount() == null) {
            taskEntity.setIndexCount(0);
        }
        if (taskEntity.getCron() != null && !taskEntity.getCron().isEmpty()) {
            CronExpression expression = CronExpression.parse(taskEntity.getCron());
            LocalDateTime now = LocalDateTime.now();
            // 获取下一次执行时间
            LocalDateTime nextExecution = expression.next(now);
            if (nextExecution == null) {
                logger.error("cron 表达式错误");
                return;
            }
            nextTime = nextExecution.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        } else {
            String time = taskEntity.getTime();
            if (time == null || time.isEmpty()) {
                logger.error("任务创建失败 cron 与 time 字段不能同时为空");
                return;
            }
            char lastChar = time.charAt(time.length() - 1);
            double timeDouble = Double.parseDouble(time.substring(0, time.length() - 1));

            if (lastChar == 'h') {
                // 获取当前时间
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime afterTime = now.plusSeconds((long) (timeDouble * 3600));

                // 将 LocalDateTime 转换为带时区的对象（使用系统默认时区）
                ZonedDateTime zonedDateTime = afterTime.atZone(ZoneId.systemDefault());

                // 转换为秒级时间戳（Unix 时间戳）
                nextTime = zonedDateTime.toEpochSecond();
            } else if (lastChar == 'm') {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime afterTime = now.plusSeconds((long) (timeDouble * 60));
                ZonedDateTime zonedDateTime = afterTime.atZone(ZoneId.systemDefault());
                nextTime = zonedDateTime.toEpochSecond();
            } else if (lastChar == 's') {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime afterTime = now.plusSeconds((long) timeDouble);
                ZonedDateTime zonedDateTime = afterTime.atZone(ZoneId.systemDefault());
                nextTime = zonedDateTime.toEpochSecond();
            }
        }
        if (nextTime != 0L) {
            redisService.setZSet(TaskConstant.TASK_QUEUE, JSONObject.toJSONString(taskEntity), nextTime);
            TaskInfo taskInfo = new TaskInfo();
            redisService.setList(TaskConstant.TASK_INFO, JSONObject.toJSONString(taskInfo));
        }
    }


}
