package com.blog.timer.registry;

import com.blog.timer.handle.TimerHandle;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public class MemoryTimerTaskRegistry implements TimerTaskRegistry {

    /**
     * 当前注册任务
     */
    private final Map<String, TimerHandle> handles = new ConcurrentHashMap<>();

    /**
     *
     */
    @Override
    public void register(TimerHandle handle) {
        handles.put(handle.getTaskId(), handle);
    }

    /**
     *
     */
    @Override
    public void unregister(String taskId) {
        handles.remove(taskId);
    }

    /**
     *
     */
    @Override
    public TimerHandle get(String taskId) {
        return handles.get(taskId);
    }

    /**
     *
     */
    @Override
    public boolean contains(String taskId) {
        return handles.containsKey(taskId);
    }

    /**
     *
     */
    @Override
    public int size() {
        return handles.size();
    }

    /**
     *
     */
    @Override
    public Collection<TimerHandle> list() {
        return handles.values();
    }

}
