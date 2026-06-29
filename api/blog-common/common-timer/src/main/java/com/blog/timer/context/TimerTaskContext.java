package com.blog.timer.context;

import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description 定时任务上下文
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Getter
public class TimerTaskContext {

    /**
     * 上下文参数。
     * -- GETTER --
     *  返回全部参数。
     *
     * @return 参数集合

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
     * 复制上下文
     * 仅复制参数容器（Map），
     * Map 中存放的对象仍共享引用。
     */
    public TimerTaskContext snapshot() {
        TimerTaskContext context = new TimerTaskContext();
        context.data.putAll(this.data);
        return context;
    }

}
