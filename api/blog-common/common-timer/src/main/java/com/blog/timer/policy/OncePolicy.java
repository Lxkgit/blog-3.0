package com.blog.timer.policy;

import java.time.Duration;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public class OncePolicy implements SchedulePolicy {

    private final Duration delay;

    public OncePolicy(Duration delay) {
        this.delay = delay;
    }

    public Duration getDelay() {
        return delay;
    }
}
