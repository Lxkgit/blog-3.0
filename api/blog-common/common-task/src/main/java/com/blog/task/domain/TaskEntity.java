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

    private String cron;

    private Integer count;
}
