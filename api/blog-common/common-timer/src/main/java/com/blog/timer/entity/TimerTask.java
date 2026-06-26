package com.blog.timer.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-26
 */

@Data
public class TimerTask {

    /**
     * 当前实例ID
     */
    private final String taskId;

    /**
     * 任务定义
     */
    private final TimerTaskDefinition definition;

    /**
     * 当前已执行次数
     */
    private final int executeCount;

    /**
     * 创建时间
     */
    private final LocalDateTime createTime;

    /**
     * 本次触发时间
     */
    private LocalDateTime triggerTime;

    /**
     * 当前Future
     */
    private ScheduledFuture<?> future;

    public TimerTask(String taskId, TimerTaskDefinition definition, int executeCount) {
        this.taskId = taskId;
        this.definition = definition;
        this.executeCount = executeCount;
        this.createTime = LocalDateTime.now();
    }
}
