package com.blog.timer.entity.trigger;

import com.blog.timer.enums.TriggerType;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Description 指定时间触发器
 * @Author lxk
 * @CreateTime 2026-06-26
 */

public class AtTimeTrigger extends Trigger {

    /**
     * 触发时间
     */
    private final LocalDateTime time;

    public AtTimeTrigger(LocalDateTime time) {
        super(TriggerType.AT_TIME);
        this.time = Objects.requireNonNull(time);
    }

    @Override
    public Duration nextDelay() {
        Duration duration = Duration.between(LocalDateTime.now(), time);
        if (duration.isNegative()) {
            return null;
        }
        return duration;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
