package com.blog.task.domain;

import com.blog.core.utils.MyStringUtils;
import lombok.Data;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-31
 */

@Data
public class TaskEntity {

    private Class<?> clazz;

    private String methodName;

    private Object[] params;

    private Class<?>[] paramsClazz;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务唯一编码
     */
    private String taskUUID;

    /**
     * cron 表达式
     */
    private String cron;

    /**
     * 任务启动时间，秒不支持小数
     * 1.5h 1.5小时后执行
     * 3m   3分钟后执行
     * 50s  50秒后执行
     */
    private String time;

    /**
     * 当前执行次数
     */
    private Integer indexCount;

    /**
     * 任务执行总次数
     * -1 为无限执行
     */
    private Integer count;

    public TaskEntity() {
        // 随机生成任务id
        this.taskUUID = MyStringUtils.getRandomString(32);
    }

    public TaskEntity(String taskUUID) {
        this.taskUUID = taskUUID;
    }
}
