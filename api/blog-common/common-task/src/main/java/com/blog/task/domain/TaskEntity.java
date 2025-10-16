package com.blog.task.domain;

import com.blog.core.utils.MyStringUtils;
import lombok.Data;

import java.util.List;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-31
 */

@Data
public class TaskEntity {

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
     * 任务名称
     */
    private String taskName;

    /**
     * 子任务id
     */
    private String childTaskCode;

    /**
     * 任务执行方法参数
     */
    private List<Object> taskParams;

    /**
     * 任务所属用户id
     */
    private Integer userId;

    /**
     * 任务状态
     * 1 正常
     * 0 停用
     */
    private Integer taskStatus;

    /**
     * cron 表达式
     */
    private String taskCron;

    /**
     * 任务启动时间，秒不支持小数
     * 1.5h 1.5小时后执行
     * 3m   3分钟后执行
     * 50s  50秒后执行
     */
    private String taskTime;

    /**
     * 当前执行次数
     */
    private Integer indexCount;

    /**
     * 任务执行总次数
     * -1 为无限执行
     */
    private Integer taskCount;

    public TaskEntity() {
        // 随机生成任务id
        this.taskCode = MyStringUtils.getRandomString(32);
    }

    public TaskEntity(String taskUUID) {
        this.taskCode = taskUUID;
    }
}
