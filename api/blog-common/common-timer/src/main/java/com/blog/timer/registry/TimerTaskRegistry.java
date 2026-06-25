package com.blog.timer.registry;

import com.blog.timer.handle.TimerHandle;

import java.util.Collection;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public interface TimerTaskRegistry {

    /**
     * 注册任务
     */
    void register(TimerHandle handle);

    /**
     * 获取任务
     */
    TimerHandle get(String taskId);

    /**
     * 是否存在
     */
    boolean contains(String taskId);

    /**
     * 任务执行完成从注册表中删除
     */
    TimerHandle unregister(String taskId);

    /**
     * 取消任务
     */
    boolean cancel(String taskId);

    /**
     * 当前任务数量
     */
    int size();

    /**
     * 获取所有任务
     */
    Collection<TimerHandle> list();

    /**
     * 获取所有任务
     */
    Collection<TimerHandle> list(String taskCode);

}
