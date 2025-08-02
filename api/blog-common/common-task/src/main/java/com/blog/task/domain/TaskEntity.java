package com.blog.task.domain;

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

    private String params;

    private Class<?> paramsClazz;

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
     * 任务执行次数
     */
    private Integer count;
}
