package com.blog.file.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.common.TaskMsgHead;
import com.blog.core.domain.file.task.entity.TaskUuid;
import com.blog.file.mapper.TaskUuidMapper;
import com.blog.file.service.TaskLogService;
import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTask;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private TaskLogService taskLogService;

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

        taskLogService.registerTaskLog(timerTask);
    }

    @Override
    public void execute(TimerTask timerTask) {
        try {
            taskLogService.taskStartLog(timerTask);
            TaskMsgHead taskMsgHead = TaskMsgHead.builder()
                    .userId(timerTask.getDefinition().getUserId())
                    .taskUuid(timerTask.getUuid())
                    .taskCode(timerTask.getDefinition().getAction().getCode())
                    .build();

            String result = doExecute(timerTask.getDefinition().getContext(), taskMsgHead);
            taskLogService.taskSuccessLog(timerTask, result);
        } catch (Exception e) {
            taskLogService.taskFailureLog(timerTask, e);
            logger.error("定时任务 {} 执行异常: uuid: {} 异常信息: {}",
                    timerTask.getDefinition().getAction().getName(), timerTask.getUuid(), e.getMessage(), e);
        } finally {
            taskLogService.taskEndLog(timerTask);
        }
    }

    protected abstract String doExecute(TimerTaskContext context, TaskMsgHead taskMsgHead);

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


}
