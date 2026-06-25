package com.blog.timer.policy;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public class CronPolicy implements SchedulePolicy {

    private final String cron;

    public CronPolicy(String cron) {
        this.cron = cron;
    }
}