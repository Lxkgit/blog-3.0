package com.blog.timer.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public class TimerTaskContext {

    private final Map<String, Object> data = new ConcurrentHashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

}
