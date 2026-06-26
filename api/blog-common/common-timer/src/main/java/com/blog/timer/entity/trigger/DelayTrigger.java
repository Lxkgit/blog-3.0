package com.blog.timer.entity.trigger;

import com.blog.timer.enums.TriggerType;
import lombok.Data;
import lombok.Getter;

import java.time.Duration;
import java.util.Objects;

/**
 * @Description 延时触发器
 * @Author lxk
 * @CreateTime 2026-06-26
 */

public class DelayTrigger extends Trigger {

    /**
     * 延时时间
     */
    private final Duration delay;

    public DelayTrigger(Duration delay) {
        super(TriggerType.DELAY);
        this.delay = Objects.requireNonNull(delay);
    }

    @Override
    public Duration nextDelay() {
        return delay;
    }

    public Duration getDelay() {
        return delay;
    }

}
