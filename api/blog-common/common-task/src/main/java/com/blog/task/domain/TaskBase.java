package com.blog.task.domain;

import lombok.Data;

import java.util.List;

/**
 * @Description 基础任务
 * @Author lxk
 * @CreateTime 2025-09-16
 */

@Data
public class TaskBase {

    /**
     * 基础任务唯一编码
     */
    private String taskCode;

    /**
     * 任务执行方法所属类
     */
    private Class<?> clazz;

    /**
     * 任务执行方法名称
     */
    private String methodName;

    /**
     * 任务执行方法参数类
     */
    private List<Class<?>> paramsClazz;

    /**
     * 任务参数配置模板
     */
    private String paramTemplate;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 是否允许手动创建子任务
     * 1 是
     * 0 否
     */
    private Integer childTaskFlag;
}
