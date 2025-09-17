package com.blog.file.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.constant.Constant;
import com.blog.core.domain.file.task.bo.SyncDeviceFileBo;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.file.mapper.TaskLogMapper;
import com.blog.file.mapper.TaskParamMapper;
import com.blog.file.service.TaskService;
import com.blog.redis.service.RedisService;
import com.blog.task.constant.TaskConstant;
import com.blog.task.domain.TaskBase;
import com.blog.task.domain.TaskEntity;
import com.blog.task.service.CreateTaskService;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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
    private TaskParamMapper taskParamMapper;

    @Resource
    private CreateTaskService createTaskService;

    @Resource
    private RedisService redisService;

    @Override
    public void updateTask(TaskParamVo taskParamVo) {
        taskParamMapper.updateById(taskParamVo);

//        List<Object> taskBaseList = redisService.getList(TaskConstant.TASK_BASE, 0, -1);
//        for (Object taskBaseObj : taskBaseList) {
//            TaskBase taskBase = (TaskBase) taskBaseObj;
//            if (taskBase.getTaskUUID().equals(taskParamVo.getTaskUUID())) {
//                List<Object> taskList = redisService.getList(TaskConstant.TASK_QUEUE, 0, -1);
//                redisService.delKey(TaskConstant.TASK_QUEUE);
//                for (Object o : taskList) {
//                    TaskEntity taskEntity = (TaskEntity) o;
//                    if (taskEntity.getTaskParamId().equals(taskParamVo.getId())) {
//                        taskEntity.setTaskCron(taskParamVo.getTaskCron());
//                        taskEntity.setTaskTime(taskParamVo.getTaskTime());
//
////                taskEntity.setTaskParams();
//                    }
//                }
//                redisService.setList(TaskConstant.TASK_QUEUE, taskList);
//            }
//        }
    }

    @Override
    public List<Object> selectTaskBaseList() {
        return redisService.getList(TaskConstant.TASK_BASE, 0, -1);
    }

    @Override
    public List<TaskParam> selectTaskEntityById(TaskParamVo taskParamVo) {
        return taskParamMapper.selectTaskByTaskUUID(taskParamVo);
    }

    @Override
    public List<TaskLog> selectTaskLogList(TaskLogVo taskLogVo) {
        PageHelper.startPage(taskLogVo.getPageNum(), taskLogVo.getPageNum());
        return taskLogMapper.selectList(null);
    }
}
