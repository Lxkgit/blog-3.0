package com.blog.timer.manager;

import com.alibaba.fastjson2.JSONObject;
import com.blog.timer.action.TimerAction;
import com.blog.timer.handle.DefaultTimerHandle;
import com.blog.timer.handle.TimerHandle;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.registry.TimerTaskRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public class DefaultTimerManager implements TimerManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTimerManager.class);

    // 负责执行信息
    private final Map<String, TimerTaskDefinition> taskStore = new ConcurrentHashMap<>();

    private final ScheduledThreadPoolExecutor executor;

    private final TimerTaskRegistry registry;

    public DefaultTimerManager(int corePoolSize, TimerTaskRegistry registry) {
        this.executor = new ScheduledThreadPoolExecutor(corePoolSize);
        this.registry = registry;
    }

    @Override
    public TimerHandle schedule(Duration delay, String taskCode, TimerAction action) {
        return schedule(delay, taskCode, action, new TimerTaskContext());
    }

    @Override
    public TimerHandle schedule(Duration delay, String taskCode, TimerAction action, TimerTaskContext context) {
        String taskId = UUID.randomUUID().toString();
//        taskStore.put(taskId, new TimerTaskDefinition(action, context, taskCode));
        ScheduledFuture<?> future = executor.schedule(() -> {
                    try {
                        action.execute(context);
                    } catch (Exception e) {
                        logger.error("定时任务执行异常, taskId: {}", taskId, e);
                    } finally {
                        registry.unregister(taskId);
                        taskStore.remove(taskId);
                    }
                }, delay.toMillis(), TimeUnit.MILLISECONDS
        );

        // 任务执行时间
        LocalDateTime triggerTime = LocalDateTime.now().plus(delay);
        TimerHandle handle = new DefaultTimerHandle(taskId, taskCode, triggerTime, future);
        logger.info("任务注册, taskId: {}, context: {}", taskId, JSONObject.toJSONString(context.getData()));
        registry.register(handle);
        return handle;
    }

    @Override
    public TimerHandle schedule(String cron, String taskCode, TimerAction action) {
        return schedule(getDelay(cron), taskCode, action, new TimerTaskContext());
    }

    @Override
    public TimerHandle schedule(String cron, String taskCode, TimerAction action, TimerTaskContext context) {
        return schedule(getDelay(cron), taskCode, action, context);
    }

    @Override
    public TimerHandle schedule(LocalDateTime time, String taskCode, TimerAction action) {
        return schedule(Duration.between(LocalDateTime.now(), time), taskCode, action, new TimerTaskContext());
    }

    @Override
    public TimerHandle schedule(LocalDateTime time, String taskCode, TimerAction action, TimerTaskContext context) {
        return schedule(Duration.between(LocalDateTime.now(), time), taskCode, action, context);
    }

    @Override
    public void executeNow(String taskId) {
        TimerTaskDefinition def = taskStore.get(taskId);
        if (def == null) {
            return;
        }
        // 1. 取消原定时任务
        TimerHandle handle = registry.get(taskId);
        if (handle != null) {
            handle.cancel();
            registry.unregister(taskId);
        }
        // 2. 清理任务定义（避免重复执行）
        taskStore.remove(taskId);
        // 3. 立即执行
        executor.execute(() -> def.getAction().execute(def.getContext()));
    }

    public Duration getDelay(String cron) {
        CronExpression expression = CronExpression.parse(cron);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = expression.next(now);
        return Duration.between(now, next);
    }

}
