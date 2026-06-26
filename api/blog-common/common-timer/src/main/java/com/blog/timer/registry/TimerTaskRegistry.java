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
     * 注册任务。
     *
     * @param handle 任务句柄
     */
    void register(TimerHandle handle);

    /**
     * 注销任务。
     *
     * @param taskId 任务实例ID
     */
    void unregister(String taskId);

    /**
     * 获取任务句柄。
     *
     * @param taskId 任务实例ID
     * @return TimerHandle
     */
    TimerHandle get(String taskId);

    /**
     * 判断任务是否存在。
     *
     * @param taskId 任务实例ID
     * @return true 存在
     */
    boolean contains(String taskId);

    /**
     * 当前任务数量。
     *
     * @return 数量
     */
    int size();

    /**
     * 获取全部任务。
     *
     * @return Collection
     */
    Collection<TimerHandle> list();

}
