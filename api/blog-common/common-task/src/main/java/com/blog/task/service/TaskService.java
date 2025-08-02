package com.blog.task.service;

import com.blog.redis.service.RedisService;
import com.blog.task.domain.TaskEntity;
import com.blog.task.utils.CronUtil;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-31
 */

public class TaskService {

    private static final String taskKey = "task.queue";

    @Resource
    private RedisService redisService;

    public void createTask(TaskEntity taskEntity) {
        long nextTime = 0L;
        if (taskEntity.getCron() != null && !taskEntity.getCron().isEmpty()) {
            nextTime = CronUtil.getCronNextTimeEpoch(taskEntity.getCron());
        } else {
            String time = taskEntity.getTime();
            char lastChar = time.charAt(time.length() - 1);
            double timeDouble = Double.parseDouble(time.substring(0, time.length() - 2));

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
            redisService.setZSet(taskKey, taskEntity, nextTime);
        }


    }


}
