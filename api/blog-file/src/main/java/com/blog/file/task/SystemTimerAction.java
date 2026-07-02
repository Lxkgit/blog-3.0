package com.blog.file.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.entity.TaskUuid;
import com.blog.file.mapper.TaskLogMapper;
import com.blog.file.mapper.TaskUuidMapper;
import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTask;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @Description 系统定时任务类
 * @Author lxk
 * @CreateTime 2026-07-01
 */


public abstract class SystemTimerAction implements TimerAction {

    private static final Logger logger = LoggerFactory.getLogger(SystemTimerAction.class);

    @Resource
    private TaskUuidMapper taskUuidMapper;

    @Resource
    private TaskLogMapper taskLogMapper;

    @Override
    public void checkParam(TimerAction action, String json) {
        logger.info("开始校验参数: {}", action.getParamTemplate());




    }

    @Override
    public void beforeExecute(TimerTask timerTask) {
        // 记录任务 uuid 于 任务参数表id 映射
        TaskUuid taskUuid = new TaskUuid();
        taskUuid.setTaskId(timerTask.getDefinition().getId());
        taskUuid.setUuid(timerTask.getUuid());
        taskUuidMapper.insert(taskUuid);

        registerTaskLog(timerTask);
    }

    @Override
    public void execute(TimerTask timerTask) {
        try {
            taskStartLog(timerTask);
            doExecute(timerTask.getDefinition().getContext());
        } catch (Exception e) {
            taskErrorLog(timerTask, e);
            logger.error("定时任务 {} 执行异常: uuid: {} 异常信息: {}",
                    timerTask.getDefinition().getAction().getName(), timerTask.getUuid(), e.getMessage(), e);
        } finally {
            taskEndLog(timerTask);
        }
    }

    protected abstract void doExecute(TimerTaskContext context);

    @Override
    public void afterExecute(TimerTask timerTask) {
        LambdaQueryWrapper<TaskUuid> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskUuid::getUuid, timerTask.getUuid());
        taskUuidMapper.delete(wrapper);
    }

    @Override
    public void finalExecute(TimerTask timerTask) {
        logger.info("任务结束");
    }

    /**
     * 注册任务日志
     *
     * @param timerTask
     */
    private void registerTaskLog(TimerTask timerTask) {
        // 任务注册日志
        TaskLog taskLog = TaskLog.builder()
                .taskName(timerTask.getDefinition().getAction().getName())
                .taskId(timerTask.getDefinition().getId())
                .taskCode(timerTask.getDefinition().getTaskCode())
                .taskUuid(timerTask.getUuid())
                .taskParam(timerTask.getDefinition().getContext().snapshot().getData().toString())
                .taskLogType(1)
                .indexCount(timerTask.getExecuteCount())
                .taskCount(timerTask.getDefinition().getPolicy().getExecuteCount())
                .build();
        taskLogMapper.insert(taskLog);
    }

    /**
     * 记录任务开始时间
     *
     * @param timerTask
     */
    private void taskStartLog(TimerTask timerTask) {
        TaskLog taskLog = TaskLog.builder().startTime(new Date()).taskResultStatus(1).build();
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskUuid, timerTask.getUuid());
        wrapper.eq(TaskLog::getTaskLogType, 1);
        taskLogMapper.update(taskLog, wrapper);
    }

    /**
     * 记录任务开始时间
     *
     * @param timerTask
     */
    private void taskErrorLog(TimerTask timerTask, Exception e) {
        TaskLog taskLog = TaskLog.builder().errorMsg(e.getMessage()).taskResultStatus(0).build();
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskUuid, timerTask.getUuid());
        wrapper.eq(TaskLog::getTaskLogType, 1);
        taskLogMapper.update(taskLog, wrapper);
    }

    /**
     * 记录任务结束时间
     *
     * @param timerTask
     */
    private void taskEndLog(TimerTask timerTask) {
        TaskLog taskLog = TaskLog.builder().endTime(new Date()).build();
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskUuid, timerTask.getUuid());
        wrapper.eq(TaskLog::getTaskLogType, 1);
        taskLogMapper.update(taskLog, wrapper);
    }


}
