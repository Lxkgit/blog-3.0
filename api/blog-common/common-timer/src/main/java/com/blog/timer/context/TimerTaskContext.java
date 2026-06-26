package com.blog.timer.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 定时任务上下文
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public class TimerTaskContext {

    /**
     * 上下文参数。
     */
    private final Map<String, Object> data = new ConcurrentHashMap<>();

    /**
     * 保存参数。
     *
     * @param key 参数名称
     * @param value 参数值
     */
    public void put(String key, Object value) {
        data.put(key, value);
    }

    /**
     * 获取参数。
     *
     * @param key 参数名称
     * @return 参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    /**
     * 返回全部参数。
     *
     * @return 参数集合
     */
    public Map<String, Object> getData() {
        return data;
    }

}
