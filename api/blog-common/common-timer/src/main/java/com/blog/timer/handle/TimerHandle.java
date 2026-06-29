package com.blog.timer.handle;

import com.blog.timer.entity.TimerTaskDefinition;

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

    TimerTaskDefinition getTaskDefinition();
//    boolean cancel();
}
