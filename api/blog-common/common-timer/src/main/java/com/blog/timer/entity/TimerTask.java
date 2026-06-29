package com.blog.timer.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
     * 任务执行记录随机id
     */
    private final String uuid;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createTime;

    /**
     * 本次触发时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime triggerTime;

    /**
     * 当前Future
     */
    @JsonIgnore
    private ScheduledFuture<?> future;

    public TimerTask(String taskId, TimerTaskDefinition definition, int executeCount) {
        this.uuid = taskId;
        this.definition = definition;
        this.executeCount = executeCount;
        this.createTime = LocalDateTime.now();
    }
}
