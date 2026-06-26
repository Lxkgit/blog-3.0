package com.blog.timer.manager;

import com.alibaba.fastjson2.JSONObject;
import com.blog.timer.action.TimerAction;
import com.blog.timer.entity.TimerTask;
import com.blog.timer.entity.TimerTaskDefinition;
import com.blog.timer.entity.policy.Policy;
import com.blog.timer.entity.trigger.AtTimeTrigger;
import com.blog.timer.entity.trigger.CronTrigger;
import com.blog.timer.entity.trigger.DelayTrigger;
import com.blog.timer.entity.trigger.Trigger;
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
import java.util.Collection;
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

    /**
     * 调度线程池
     */
    private final ScheduledThreadPoolExecutor executor;

    /**
     * 任务注册中心
     */
    private final TimerTaskRegistry registry;

    /**
     * 当前运行中的任务
     */
    private final Map<String, TimerTask> runningTasks = new ConcurrentHashMap<>();

    public DefaultTimerManager(int poolSize, TimerTaskRegistry registry) {
        this.executor = new ScheduledThreadPoolExecutor(poolSize);
        this.registry = registry;
    }

    /**
     * 手动调用启动任务入口
     */
    @Override
    public TimerHandle schedule(TimerTaskDefinition definition) {
        return schedule(definition, 1);

    }

    /**
     * 创建新的任务实例
     *
     * @param definition   任务定义
     * @param executeCount 当前执行次数
     * @return TimerHandle 任务实例
     */
    private TimerHandle schedule(TimerTaskDefinition definition, int executeCount) {
        String taskId = UUID.randomUUID().toString();
        Duration delay = calculateDelay(definition.getTrigger());
        if (delay.isNegative()) {
            throw new IllegalArgumentException("触发时间已过");
        }
        TimerTask task = new TimerTask(taskId, definition, executeCount);
        task.setTriggerTime(LocalDateTime.now().plus(delay));
        ScheduledFuture<?> future = executor.schedule(() -> execute(task), delay.toMillis(), TimeUnit.MILLISECONDS);

        task.setFuture(future);
        runningTasks.put(taskId, task);

        TimerHandle handle = new DefaultTimerHandle(taskId, definition.getTaskCode(), task.getTriggerTime());

        registry.register(handle);

        logger.info("任务注册，taskId：{}，taskCode：{}，triggerTime：{}，context：{}",
                taskId, definition.getTaskCode(), task.getTriggerTime(), JSONObject.toJSONString(definition.getContext().getData()));

        return handle;
    }

    /**
     * 执行任务
     *
     * @param task 当前任务实例
     */
    @SuppressWarnings("unchecked")
    private void execute(TimerTask task) {
        TimerTaskDefinition definition = task.getDefinition();
        TimerAction action = definition.getAction();
        try {
            action.execute(definition.getContext());
//            logger.info("任务执行完成，taskId：{}，result：{}", task.getTaskId(), JSONObject.toJSONString(result));
        } catch (Exception e) {
            logger.error("任务执行异常，taskId：{}", task.getTaskId(), e);
        } finally {
            afterExecute(task);
        }
    }

    /**
     * 任务执行完成后的生命周期处理。
     *
     * @param task 当前任务
     */
    private void afterExecute(TimerTask task) {
        runningTasks.remove(task.getTaskId());
        registry.unregister(task.getTaskId());
        Policy policy = task.getDefinition().getPolicy();
        if (policy.shouldContinue(task.getExecuteCount())) {
            schedule(task.getDefinition(), task.getExecuteCount() + 1);
        }
    }

    /**
     * 创建下一次任务实例。
     *
     * @param currentTask 当前任务
     */
    private void scheduleNext(TimerTask currentTask) {
        schedule(currentTask.getDefinition(), currentTask.getExecuteCount() + 1);

    }

    @Override
    public void executeNow(String taskId) {
        TimerTask task = runningTasks.get(taskId);
        if (task == null) {
            return;
        }
        ScheduledFuture<?> future = task.getFuture();
        // 已经开始执行
        if (future.isDone()) {
            return;
        }
        future.cancel(false);
        executor.execute(() -> execute(task));
    }


    @Override
    public boolean cancel(String taskId) {
        TimerTask task = runningTasks.remove(taskId);
        if (task == null) {
            return false;
        }
        registry.unregister(taskId);
        logger.info("任务取消，taskId：{}", taskId);
        return task.getFuture().cancel(false);
    }

    /**
     * 根据 Trigger 计算等待时间。
     *
     * @param trigger Trigger
     * @return Duration
     */
    private Duration calculateDelay(Trigger trigger) {
        if (trigger instanceof DelayTrigger delayTrigger) {
            return delayTrigger.getDelay();
        }
        if (trigger instanceof AtTimeTrigger atTimeTrigger) {
            return Duration.between(LocalDateTime.now(), atTimeTrigger.getTime());
        }
        if (trigger instanceof CronTrigger cronTrigger) {
            return getDelay(cronTrigger.getCron());
        }
        throw new IllegalArgumentException("未知 Trigger 类型：" + trigger.getClass());
    }

    @Override
    public TimerTask get(String taskId) {
        return runningTasks.get(taskId);
    }

    @Override
    public Collection<TimerTask> list() {
        return runningTasks.values();
    }

    @Override
    public int size() {
        return runningTasks.size();
    }

    /**
     * 计算 Cron 下一次执行时间。
     *
     * @param cron Cron表达式
     * @return Duration
     */
    private Duration getDelay(String cron) {
        throw new UnsupportedOperationException("暂未实现");
    }

}


//public class DefaultTimerManager implements TimerManager {
//
//    private static final Logger logger = LoggerFactory.getLogger(DefaultTimerManager.class);
//
//    // 负责执行信息
//    private final Map<String, TimerTaskDefinition> taskStore = new ConcurrentHashMap<>();
//
//    private final ScheduledThreadPoolExecutor executor;
//
//    private final TimerTaskRegistry registry;
//
//    public DefaultTimerManager(int corePoolSize, TimerTaskRegistry registry) {
//        this.executor = new ScheduledThreadPoolExecutor(corePoolSize);
//        this.registry = registry;
//    }
//
//    @Override
//    public TimerHandle schedule(TimerTaskDefinition definition) throws Exception {
//        String taskId = UUID.randomUUID().toString();
//        String taskCode = definition.getTaskCode();
//        TimerAction action = definition.getAction();
//        TimerTaskContext context = definition.getContext();
//
//        Duration delay;
//        if (definition.getTrigger() instanceof DelayTrigger) {
//            delay = ((DelayTrigger) definition.getTrigger()).getDelay();
//        } else if (definition.getTrigger() instanceof AtTimeTrigger) {
//            delay = Duration.between(LocalDateTime.now(), ((AtTimeTrigger) definition.getTrigger()).getTime());
//        } else if (definition.getTrigger() instanceof CronTrigger) {
//            delay = getDelay(((CronTrigger) definition.getTrigger()).getCron());
//        } else {
//            throw new Exception();
//        }
//
//        taskStore.put(taskId, definition);
//
//        ScheduledFuture<?> future = executor.schedule(() -> {
//                    try {
//                        action.execute(context);
//                    } catch (Exception e) {
//                        logger.error("定时任务执行异常, taskId: {}", taskId, e);
//                    } finally {
//                        registry.unregister(taskId);
//                        taskStore.remove(taskId);
//                    }
//                }, delay.toMillis(), TimeUnit.MILLISECONDS
//        );
//
//        // 任务执行时间
//        LocalDateTime triggerTime = LocalDateTime.now().plus(delay);
//        TimerHandle handle = new DefaultTimerHandle(taskId, taskCode, triggerTime, future);
//        logger.info("任务注册, taskId: {}, context: {}", taskId, JSONObject.toJSONString(context.getData()));
//        registry.register(handle);
//        return handle;
//    }
//
//    private int executeCount;
//
//    private int maxExecuteCount;
//
//
//
//    @Override
//    public void executeNow(String taskId) {
//        TimerTaskDefinition def = taskStore.get(taskId);
//        if (def == null) {
//            return;
//        }
//        // 1. 取消原定时任务
//        TimerHandle handle = registry.get(taskId);
//        if (handle != null) {
//            handle.cancel();
//            registry.unregister(taskId);
//        }
//        // 2. 清理任务定义（避免重复执行）
//        taskStore.remove(taskId);
//        // 3. 立即执行
//        executor.execute(() -> def.getAction().execute(def.getContext()));
//    }
//
//    public Duration getDelay(String cron) {
//        CronExpression expression = CronExpression.parse(cron);
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime next = expression.next(now);
//        return Duration.between(now, next);
//    }
//
//}
