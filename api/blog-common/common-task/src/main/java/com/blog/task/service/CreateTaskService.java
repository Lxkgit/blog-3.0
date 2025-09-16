package com.blog.task.service;

import com.alibaba.fastjson2.JSONObject;
import com.blog.redis.service.RedisService;
import com.blog.task.constant.TaskConstant;
import com.blog.task.domain.TaskEntity;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


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
        if (taskEntity.getTaskCron() != null && !taskEntity.getTaskCron().isEmpty()) {
            CronExpression expression = CronExpression.parse(taskEntity.getTaskCron());
            LocalDateTime now = LocalDateTime.now();
            // 获取下一次执行时间
            LocalDateTime nextExecution = expression.next(now);
            if (nextExecution == null) {
                logger.error("cron 表达式错误");
                return;
            }
            nextTime = nextExecution.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        } else {
            String time = taskEntity.getTaskTime();
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

            List<Object> taskList = redisService.getList(TaskConstant.TASK_ENTITY, 0, -1);

            Set<String> UUIDSet = taskList.stream().filter(obj -> obj instanceof TaskEntity)
                    .map(obj -> (TaskEntity) obj).map(TaskEntity::getTaskUUID).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(UUIDSet)) {
                redisService.setList(TaskConstant.TASK_ENTITY, JSONObject.toJSONString(taskEntity));
            }
            if (!CollectionUtils.isEmpty(UUIDSet) && !UUIDSet.contains(taskEntity.getTaskUUID())) {
                redisService.setList(TaskConstant.TASK_ENTITY, JSONObject.toJSONString(taskEntity));
            }
            // 任务状态为0的任务不创建执行队列
            if (taskEntity.getTaskStatus() != null && taskEntity.getTaskStatus() == 0) {
                return;
            }
            redisService.setZSet(TaskConstant.TASK_QUEUE, JSONObject.toJSONString(taskEntity), nextTime);
        }
    }


}
