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

    private final Map<String, TimerHandle> taskMap = new ConcurrentHashMap<>();

    @Override
    public void register(TimerHandle handle) {
        taskMap.put(handle.getTaskId(), handle);
    }

    @Override
    public TimerHandle get(String taskId) {
        return taskMap.get(taskId);
    }

    @Override
    public boolean contains(String taskId) {
        return taskMap.containsKey(taskId);
    }

    @Override
    public TimerHandle unregister(String taskId) {
        return taskMap.remove(taskId);
    }

    @Override
    public boolean cancel(String taskId) {
        TimerHandle handle = taskMap.remove(taskId);
        if (handle == null) {
            return false;
        }
        return handle.cancel();
    }

    @Override
    public int size() {
        return taskMap.size();
    }

    @Override
    public Collection<TimerHandle> list() {
        return taskMap.values();
    }

    @Override
    public Collection<TimerHandle> list(String taskCode) {
        return taskMap.values().stream().filter(handle -> handle.getTaskCode().equals(taskCode)).collect(Collectors.toList());
    }
}
