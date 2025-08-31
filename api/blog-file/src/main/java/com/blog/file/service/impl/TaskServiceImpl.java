package com.blog.file.service.impl;

import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.file.mapper.TaskLogMapper;
import com.blog.file.service.TaskService;
import com.blog.redis.service.RedisService;
import com.blog.task.constant.TaskConstant;
import com.blog.task.domain.TaskEntity;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 任务服务实现类
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@Service
public class TaskServiceImpl implements TaskService {

    @Resource
    private TaskLogMapper taskLogMapper;

    @Resource
    private RedisService redisService;

    @Override
    public void updateTask(TaskEntity taskInfoParam) {
        List<Object> taskList = redisService.getList(TaskConstant.TASK_ENTITY, 0, -1);
        for (int i = 0; i< taskList.size(); i++) {
            TaskEntity taskEntity = (TaskEntity) taskList.get(i);
            if (taskEntity.getTaskUUID().equals(taskInfoParam.getTaskUUID())) {
                taskEntity.setParams(taskInfoParam.getParams());
                taskEntity.setCron(taskInfoParam.getCron());
                taskEntity.setTime(taskInfoParam.getTime());
                taskEntity.setCount(taskInfoParam.getCount());
                taskEntity.setTaskStatus(taskInfoParam.getTaskStatus());
            }
            redisService.updateListByIndex(TaskConstant.TASK_ENTITY, i, taskEntity);
        }

    }

    @Override
    public List<Object> selectTaskInfoList() {
        return redisService.getList(TaskConstant.TASK_ENTITY, 0, -1);
    }

    @Override
    public List<TaskLog> selectTaskLogList(TaskLogVo taskLogVo) {
        PageHelper.startPage(taskLogVo.getPageNum(), taskLogVo.getPageNum());
        return taskLogMapper.selectList(null);
    }
}
