package com.blog.timer.manager;

import com.blog.timer.entity.TimerTask;
import com.blog.timer.entity.TimerTaskDefinition;
import com.blog.timer.handle.TimerHandle;

import java.util.Collection;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public interface TimerManager {

    /**
     * 创建一个新的定时任务。
     *
     * @param definition 任务定义
     * @return 任务句柄
     */
    TimerHandle schedule(TimerTaskDefinition definition);

    /**
     * 立即执行指定任务
     * 立即执行后，将根据任务策略重新创建下一次任务
     * 或结束任务生命周期。
     *
     * @param uuid 任务uuid
     */
    void executeNow(String uuid);

    /**
     * 取消任务
     *
     * @param uuid 任务uuid
     * @return true 成功
     */
    boolean cancel(String uuid);

    /**
     * 获取任务实例。
     *
     * @param uuid 任务uuid
     * @return TimerTask
     */
    TimerTask get(String uuid);

    /**
     * 获取全部运行中的任务。
     *
     * @return Collection
     */
    Collection<TimerTask> list();

    /**
     * 当前任务数量。
     *
     * @return 数量
     */
    int size();

}
