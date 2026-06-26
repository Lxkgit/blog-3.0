package com.blog.timer.entity;

import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.policy.Policy;
import com.blog.timer.entity.trigger.Trigger;
import lombok.Builder;
import lombok.Data;

/**
 * @Description 任务定义类
 * @Author lxk
 * @CreateTime 2026-06-26
 */

@Data
@Builder
public class TimerTaskDefinition {

    /**
     * 任务编码
     */
    private String taskCode;

    /**
     * 执行动作
     */
    private TimerAction action;

    /**
     * 任务参数
     */
    private TimerTaskContext context;

    /**
     * 触发规则
     */
    private Trigger trigger;

    /**
     * 执行策略
     */
    private Policy policy;

}
