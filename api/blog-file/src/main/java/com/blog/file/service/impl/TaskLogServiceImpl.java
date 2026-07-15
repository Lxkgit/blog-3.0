package com.blog.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.result.ResultPage;
import com.blog.core.result.ResultPageUtils;
import com.blog.file.mapper.TaskLogMapper;
import com.blog.file.service.TaskLogService;
import com.blog.timer.entity.TimerTask;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Description 定时任务日志服务实现类
 * @Author lxk
 * @CreateTime 2026-07-03
 */

@Service
public class TaskLogServiceImpl implements TaskLogService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Resource
    private TaskLogMapper taskLogMapper;

    @Override
    public ResultPage<TaskLogVo> selectTaskLogList(TaskLogVo taskLogVo) {
        PageHelper.startPage(taskLogVo.getPageNum(), taskLogVo.getPageSize());
        List<TaskLogVo> taskLogVoList = taskLogMapper.selectTaskLogList();
        return ResultPageUtils.pageUtil(taskLogVoList, taskLogVo.getPageNum(), taskLogVo.getPageSize(), new PageInfo<>(taskLogVoList).getTotal());
    }

    @Override
    public ResultPage<TaskLog> selectTaskLogByUuid(TaskLogVo taskLogVo) {
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskUuid, taskLogVo.getTaskUuid());
        PageHelper.startPage(taskLogVo.getPageNum(), taskLogVo.getPageSize());
        List<TaskLog> taskLogVoList = taskLogMapper.selectList(wrapper);
        return ResultPageUtils.pageUtil(taskLogVoList, taskLogVo.getPageNum(), taskLogVo.getPageSize(), new PageInfo<>(taskLogVoList).getTotal());
    }

    /**
     * 注册任务日志
     *
     * @param timerTask
     */
    @Override
    public void registerTaskLog(TimerTask timerTask) {
        // 任务注册日志
        TaskLog taskLog = TaskLog.builder()
                .userId(timerTask.getDefinition().getUserId())
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

    @Override
    public void executeTaskLog(MsgHead msgHead, TaskLog taskLog) {
        taskLog.setUserId(msgHead.getTaskMsgHead().getUserId());
        taskLog.setTaskCode(msgHead.getTaskMsgHead().getTaskCode());
        taskLog.setTaskUuid(msgHead.getTaskMsgHead().getTaskUuid());
        taskLog.setTaskLogType(2);
        taskLogMapper.insert(taskLog);
    }

    /**
     * 记录任务开始时间
     *
     * @param timerTask
     */
    @Override
    public void taskStartLog(TimerTask timerTask) {
        TaskLog taskLog = TaskLog.builder().startTime(new Date()).build();
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskUuid, timerTask.getUuid());
        wrapper.eq(TaskLog::getTaskLogType, 1);
        taskLogMapper.update(taskLog, wrapper);
    }

    @Override
    public void taskStartLog(Integer id) {
        TaskLog taskLog = TaskLog.builder().id(id).startTime(new Date()).build();
        taskLogMapper.updateById(taskLog);
    }

    /**
     * 记录任务成功信息
     */
    @Override
    public void taskSuccessLog(TimerTask timerTask, String result) {
        TaskLog taskLog = TaskLog.builder().taskResultStatus(1).taskResult(result).build();
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskUuid, timerTask.getUuid());
        wrapper.eq(TaskLog::getTaskLogType, 1);
        taskLogMapper.update(taskLog, wrapper);
    }

    @Override
    public void taskSuccessLog(Integer id, String result) {
        TaskLog taskLog = TaskLog.builder().id(id).taskResultStatus(1).taskResult(result).build();
        taskLogMapper.updateById(taskLog);
    }

    /**
     * 记录任务失败信息
     *
     * @param timerTask
     */
    @Override
    public void taskFailureLog(TimerTask timerTask, Exception e) {
        TaskLog taskLog = TaskLog.builder().errorMsg(e.getMessage()).taskResultStatus(0).build();
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskUuid, timerTask.getUuid());
        wrapper.eq(TaskLog::getTaskLogType, 1);
        taskLogMapper.update(taskLog, wrapper);
    }

    @Override
    public void taskFailureLog(Integer id, Exception e) {
        TaskLog taskLog = TaskLog.builder().id(id).taskResultStatus(0).taskResult(e.getMessage()).build();
        taskLogMapper.updateById(taskLog);
    }

    /**
     * 记录任务结束时间
     *
     * @param timerTask
     */
    @Override
    public void taskEndLog(TimerTask timerTask) {
        TaskLog taskLog = TaskLog.builder().endTime(new Date()).build();
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskUuid, timerTask.getUuid());
        wrapper.eq(TaskLog::getTaskLogType, 1);
        taskLogMapper.update(taskLog, wrapper);
    }

    @Override
    public void taskEndLog(String taskUuid) {
        TaskLog taskLog = TaskLog.builder().endTime(new Date()).build();
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskUuid, taskUuid);
        wrapper.eq(TaskLog::getTaskLogType, 1);
        taskLogMapper.update(taskLog, wrapper);
    }

    @Override
    public void taskEndLog(Integer id) {
        TaskLog taskLog = TaskLog.builder().id(id).endTime(new Date()).build();
        taskLogMapper.updateById(taskLog);
    }

    @Override
    public void completeTaskLog(TaskLog taskLog) {

    }
}
