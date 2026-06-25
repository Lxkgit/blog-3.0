package com.blog.timer.action;

import com.blog.timer.context.TimerTaskContext;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public interface TimerAction {

    String getCode();

    String getName();

    void execute(TimerTaskContext context);

}
