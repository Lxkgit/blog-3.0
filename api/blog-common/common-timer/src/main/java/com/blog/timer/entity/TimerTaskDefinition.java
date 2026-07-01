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
     * 数据库中任务id
     */
    private final Integer id;

    /**
     * 任务编码
     */
    private final String taskCode;

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

    /**
     * 创建当前任务定义的副本。
     * Action、Trigger、Policy 为不可变对象，
     * 直接共享引用即可
     * Context 会复制一份新的参数容器，
     * 防止任务创建后参数继续修改。
     */
    public TimerTaskDefinition snapshot() {
        return TimerTaskDefinition.builder()
                .id(id)
                .taskCode(taskCode)
                .action(action)
                .context(context == null ? null : context.snapshot())
                .trigger(trigger)
                .policy(policy)
                .build();
    }
}
