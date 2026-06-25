package com.blog.timer.handle;

import com.blog.timer.context.TimerTaskContext;

import java.time.LocalDateTime;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public interface TimerHandle {

    String getTaskId();

    String getTaskCode();

    LocalDateTime getTriggerTime();

    boolean cancel();
}
