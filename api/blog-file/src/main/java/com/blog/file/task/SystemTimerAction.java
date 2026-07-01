package com.blog.file.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.task.entity.TaskUuid;
import com.blog.file.mapper.TaskUuidMapper;
import com.blog.timer.action.TimerAction;
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

    @Override
    public void checkParam(String json) {
        logger.info("开始校验参数");
    }

    @Override
    public void beforeExecute(TimerTask timerTask) {
        TaskUuid taskUuid = new TaskUuid();
        taskUuid.setTaskId(timerTask.getDefinition().getId());
        taskUuid.setUuid(timerTask.getUuid());
        taskUuidMapper.insert(taskUuid);
    }

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
