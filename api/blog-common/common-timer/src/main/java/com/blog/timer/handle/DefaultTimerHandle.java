package com.blog.timer.handle;

import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.manager.TimerManager;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */


public class DefaultTimerHandle implements TimerHandle {

    private final String taskId;

    private final String taskCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private final LocalDateTime triggerTime;


    public DefaultTimerHandle(String taskId, String taskCode, LocalDateTime triggerTime) {
        this.taskId = taskId;
        this.taskCode = taskCode;
        this.triggerTime = triggerTime;

    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public String getTaskCode() {
        return taskCode;
    }

    @Override
    public LocalDateTime getTriggerTime() {
        return triggerTime;
    }

//    @Override
//    public boolean cancel() {
//        return manager.cancel(taskId);
//    }
}

//public class DefaultTimerHandle implements TimerHandle {
//
//    private final String taskId;
//
//    private final String taskCode;
//
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//    private final LocalDateTime triggerTime;
//
//    private final ScheduledFuture<?> future;
//
//    public DefaultTimerHandle(String taskId, String taskCode, LocalDateTime triggerTime, ScheduledFuture<?> future) {
//        this.taskId = taskId;
//        this.taskCode = taskCode;
//        this.triggerTime = triggerTime;
//        this.future = future;
//    }
//
//    @Override
//    public String getTaskId() {
//        return taskId;
//    }
//
//    @Override
//    public String getTaskCode() {
//        return taskCode;
//    }
//
//    @Override
//    public LocalDateTime getTriggerTime() {
//        return triggerTime;
//    }
//
//    @Override
//    public boolean cancel() {
//        return future.cancel(false);
//    }
//
//
//}