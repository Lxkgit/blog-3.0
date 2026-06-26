package com.blog.timer.entity.trigger;

import com.blog.timer.enums.TriggerType;
import lombok.Getter;

import java.time.Duration;

/**
 * @Description Cron 表达式触发器
 * @Author lxk
 * @CreateTime 2026-06-26
 */

public class CronTrigger extends Trigger {

    /**
     * Cron表达式
     */
    private final String cron;

    public CronTrigger(String cron) {
        super(TriggerType.CRON);
        this.cron = cron;
    }

    public String getCron() {
        return cron;
    }

    @Override
    public Duration nextDelay() {
        return null;
    }
}
