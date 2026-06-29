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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;

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
     * 当前运行中的任务
     */
    private final Map<String, TimerTask> runningTasks = new ConcurrentHashMap<>();

    public DefaultTimerManager(int poolSize) {
        this.executor = new ScheduledThreadPoolExecutor(poolSize);
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
        TimerTask task = new TimerTask(taskId, definition.snapshot(), executeCount);
        task.setTriggerTime(LocalDateTime.now().plus(delay));

        // 执行任务
        ScheduledFuture<?> future = executor.schedule(() -> execute(task), delay.toMillis(), TimeUnit.MILLISECONDS);
        task.setFuture(future);

        // 管理任务执行队列
        runningTasks.put(taskId, task);

        // 管理任务注册队列
        TimerHandle handle = new DefaultTimerHandle(taskId, definition.getTaskCode(), task.getTriggerTime(), task.getDefinition());

        logger.info("任务注册，taskId：{}，taskCode：{}，triggerTime：{}，context：{}",
                taskId, definition.getTaskCode(), task.getTriggerTime(), JSONObject.toJSONString(definition.getContext().getData()));

        return handle;
    }

    /**
     * 执行任务
     *
     * @param task 当前任务实例
     */
    private void execute(TimerTask task) {
        TimerTaskDefinition definition = task.getDefinition();
        TimerAction action = definition.getAction();
        try {
            action.execute(definition.getContext());
//            logger.info("任务执行完成，taskId：{}，result：{}", task.getTaskId(), JSONObject.toJSONString(result));
        } catch (Exception e) {
            logger.error("任务执行异常，taskId：{}", task.getUuid(), e);
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
        runningTasks.remove(task.getUuid());
        Policy policy = task.getDefinition().getPolicy();
        if (policy.shouldContinue(task.getExecuteCount())) {
            schedule(task.getDefinition(), task.getExecuteCount() + 1);
        }
    }

    @Override
    public void executeNow(String taskId) {
        TimerTask task = runningTasks.get(taskId);
        if (task == null) {
            logger.info("任务不存在，taskId：{}", taskId);
            return;
        }
        ScheduledFuture<?> future = task.getFuture();
        // 已经开始执行
        if (future.isDone()) {
            logger.info("任务已经开始执行，taskId：{}", taskId);
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
        logger.info("任务取消，taskId：{}", taskId);
        return task.getFuture().cancel(false);
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

    /**
     * 计算 Cron 下一次执行时间。
     *
     * @param cron Cron表达式
     * @return Duration
     */
    private Duration getDelay(String cron) {

        CronExpression expression = CronExpression.parse(cron);
        LocalDateTime now = LocalDateTime.now();
        // 获取下一次执行时间
        LocalDateTime nextExecution = expression.next(now);
        if (nextExecution == null) {
            logger.error("cron 表达式错误");
            throw new RuntimeException("cron 表达式错误");
        }
        return Duration.between(now, nextExecution);
    }
}