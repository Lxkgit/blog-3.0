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
import java.util.*;
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
        String uuid = UUID.randomUUID().toString();
        Duration delay = calculateDelay(definition.getTrigger());
        if (delay.isNegative()) {
            throw new IllegalArgumentException("触发时间已过");
        }
        TimerTask task = new TimerTask(uuid, definition.snapshot(), executeCount);
        task.setTriggerTime(LocalDateTime.now().plus(delay));

        // 任务执行前
        beforeExecute(task);

        // 执行任务
        ScheduledFuture<?> future = executor.schedule(() -> executeFlow(task), delay.toMillis(), TimeUnit.MILLISECONDS);
        task.setFuture(future);

        // 管理任务执行队列
        runningTasks.put(uuid, task);

        // 管理任务注册队列
        TimerHandle handle = new DefaultTimerHandle(uuid, definition.getTaskCode(), task.getTriggerTime(), task.getDefinition());

        logger.info("任务注册，taskId：{}，taskCode：{}，triggerTime：{}，context：{}",
                uuid, definition.getTaskCode(), task.getTriggerTime(), JSONObject.toJSONString(definition.getContext().getData()));

        return handle;
    }

    /**
     * 执行任务流程
     *
     * @param task 当前任务实例
     */
    private void executeFlow(TimerTask task) {
        execute(task);
        afterExecute(task, false);
    }

    /**
     * 任务执行前
     *
     * @param task
     */
    private void beforeExecute(TimerTask task) {
        TimerTaskDefinition definition = task.getDefinition();
        TimerAction action = definition.getAction();

        action.beforeExecute(task);
    }

    /**
     * 任务执行
     *
     * @param task
     */
    private void execute(TimerTask task) {
        TimerTaskDefinition definition = task.getDefinition();
        TimerAction action = definition.getAction();

        action.execute(task);
    }

    /**
     * 任务执行后
     *
     * @param task     任务参数
     * @param isCancel 是否取消执行
     */
    private void afterExecute(TimerTask task, boolean isCancel) {
        TimerTaskDefinition definition = task.getDefinition();
        TimerAction action = definition.getAction();

        // 任务运行队列移除当前执行任务
        runningTasks.remove(task.getUuid());
        // 任务执行后删除任务uuid与任务id映射表数据
        action.afterExecute(task);
        // 判断当前任务是否需要继续执行
        Policy policy = task.getDefinition().getPolicy();
        if (!isCancel && policy.shouldContinue(task.getExecuteCount())) {
            // 任务开始下一次循环
            schedule(task.getDefinition(), task.getExecuteCount() + 1);
        } else {
            // 任务结束
            action.finalExecute(task);
        }
    }

    @Override
    public void executeNow(String uuid) {
        TimerTask task = runningTasks.get(uuid);
        if (task == null) {
            logger.info("任务不存在，taskId：{}", uuid);
            return;
        }
        ScheduledFuture<?> future = task.getFuture();
        // 已经开始执行
        if (future.isDone()) {
            logger.info("任务已经开始执行，taskId：{}", uuid);
            return;
        }
        future.cancel(false);
        executor.execute(() -> execute(task));
        afterExecute(task, false);
    }

    @Override
    public boolean cancel(String uuid) {
        if (runningTasks.containsKey(uuid)) {
            logger.info("任务取消，uuid：{}", uuid);
            TimerTask task = runningTasks.get(uuid);
            afterExecute(task, true);
            return task.getFuture().cancel(false);
        }
        return false;
    }

    @Override
    public TimerTask getTaskByUuid(String uuid) {
        return runningTasks.get(uuid);
    }

    @Override
    public Collection<TimerTask> getAllTask() {
        return runningTasks.values();
    }

    @Override
    public int getTaskCount() {
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