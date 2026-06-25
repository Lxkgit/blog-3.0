package com.blog.timer.manager;

import com.blog.timer.action.TimerAction;
import com.blog.timer.handle.TimerHandle;
import com.blog.timer.context.TimerTaskContext;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public interface TimerManager {

    TimerHandle schedule(Duration delay, String taskCode, TimerAction action);

    TimerHandle schedule(Duration delay, String taskCode, TimerAction action, TimerTaskContext context);

    TimerHandle schedule(String cron, String taskCode, TimerAction action);

    TimerHandle schedule(String cron, String taskCode, TimerAction action, TimerTaskContext context);

    TimerHandle schedule(LocalDateTime time, String taskCode, TimerAction action);

    TimerHandle schedule(LocalDateTime time, String taskCode, TimerAction action, TimerTaskContext context);

    void executeNow(String taskId);
}
