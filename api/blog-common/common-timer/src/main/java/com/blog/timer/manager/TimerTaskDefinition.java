package com.blog.timer.manager;

import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.enums.TimerTaskType;
import lombok.Data;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Data
public class TimerTaskDefinition {

    private final String taskCode;

    private final TimerTaskType type;

    private final TimerAction action;

    private final TimerTaskContext context;

}
