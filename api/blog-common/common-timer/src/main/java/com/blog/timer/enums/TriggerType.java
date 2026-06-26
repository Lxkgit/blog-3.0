package com.blog.timer.enums;

/**
 * @Description 触发器类型
 * @Author lxk
 * @CreateTime 2026-06-26
 */

public enum TriggerType {

    /**
     * 延时执行。
     */
    DELAY,

    /**
     * 指定时间执行。
     */
    AT_TIME,

    /**
     * Cron表达式。
     */
    CRON

}