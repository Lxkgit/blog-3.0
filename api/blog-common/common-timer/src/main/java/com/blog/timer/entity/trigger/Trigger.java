package com.blog.timer.entity.trigger;

import com.blog.timer.enums.TriggerType;

import java.time.Duration;

/**
 * @Description 任务触发规则
 * @Author lxk
 * @CreateTime 2026-06-26
 */

public abstract class Trigger {

    /**
     * 触发器类型
     */
    private final TriggerType type;

    protected Trigger(TriggerType type) {
        this.type = type;
    }

    /**
     * 计算下一次等待时间。
     *
     * @return null 表示没有下一次执行
     */
    public abstract Duration nextDelay();

    /**
     * 获取触发器类型
     *
     * @return TriggerType
     */
    public TriggerType getType() {
        return type;
    }
}
