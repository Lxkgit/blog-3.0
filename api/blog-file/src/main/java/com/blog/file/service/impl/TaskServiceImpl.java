package com.blog.file.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.auth.entity.Role;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.core.result.ResultPage;
import com.blog.core.result.ResultPageUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.TaskLogMapper;
import com.blog.file.mapper.TaskParamMapper;
import com.blog.file.service.TaskService;
import com.blog.redis.service.RedisService;
import com.blog.task.constant.TaskConstant;
import com.blog.task.domain.TaskEntity;
import com.blog.task.service.CreateTaskService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Description 任务服务实现类
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

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
        TaskParam taskParam = taskParamMapper.selectById(taskParamVo.getId());
        Set<Object> objSet = redisService.getZSetList(TaskConstant.TASK_QUEUE, 0, -1);

        for (Object obj : objSet) {
            TaskEntity taskEntity = (TaskEntity) obj;
            if (taskEntity.getChildTaskCode().equals(taskParam.getChildTaskCode())) {
                redisService.removeZSetByMember(TaskConstant.TASK_QUEUE, obj);
                createChildTask(taskEntity, taskParam);
            }
        }


    }

    /**
     * 创建定时任务
     *
     * @param taskEntity 任务基础参数
     * @param taskParam  任务配置参数
     */
    @Override
    public void createChildTask(TaskEntity taskEntity, TaskParam taskParam) {
        try {
            taskEntity.setChildTaskCode(taskParam.getChildTaskCode());
            taskEntity.setTaskCron(taskParam.getTaskCron());
            taskEntity.setTaskTime(taskParam.getTaskTime());
            taskEntity.setTaskCount(taskParam.getTaskCount());
            // 填充任务参数
            if (StringUtils.isNotEmpty(taskParam.getParamClazz()) && StringUtils.isNotEmpty(taskParam.getParamJson())) {
                JSONArray jsonClazzArray = JSONArray.parseArray(taskParam.getParamClazz());
                JSONArray jsonParamArray = JSONArray.parseArray(taskParam.getParamJson());
                taskEntity.setTaskParams(new ArrayList<>());
                for (int i = 0; i < jsonParamArray.size(); i++) {
                    // 获取参数所属类
                    Class<?> clazz = Class.forName(jsonClazzArray.getString(i));
                    // 获取参数数据
                    JSONObject jsonObject = (JSONObject) jsonParamArray.get(i);
                    // 转为Java对象
                    Object paramObj = JSON.parseObject(jsonObject.toJSONString(), clazz);
                    taskEntity.getTaskParams().add(paramObj);
                }
            }
            createTaskService.createTask(taskEntity);
        } catch (Exception e) {
            logger.error("任务 {} 创建异常: {}", taskEntity, e.getMessage());
        }
    }

    @Override
    public List<Object> selectTaskBaseList() {
        return redisService.getList(TaskConstant.TASK_BASE, 0, -1);
    }

    @Override
    public List<TaskParam> selectTaskEntityById(String taskCode) {
        TaskParamVo taskParamVo = new TaskParamVo();
        taskParamVo.setTaskCode(taskCode);
        taskParamVo.setUserId(SecurityUtil.getLoginUser().getId());
        return taskParamMapper.selectTaskByTaskCode(taskParamVo);
    }

    @Override
    public ResultPage<TaskLogVo> selectTaskLogList(TaskLogVo taskLogVo) {
        PageHelper.startPage(taskLogVo.getPageNum(), taskLogVo.getPageSize());
        List<TaskLogVo> taskLogVoList = taskLogMapper.selectTaskLogList();
        return ResultPageUtils.pageUtil(taskLogVoList, taskLogVo.getPageNum(), taskLogVo.getPageSize(), new PageInfo<>(taskLogVoList).getTotal());
    }

    @Override
    public List<TaskLog> selectTaskLogByTaskUUID(String taskUUID) {
        LambdaQueryWrapper<TaskLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskLog::getTaskUUID, taskUUID);
        queryWrapper.orderByAsc(TaskLog::getId);
        return taskLogMapper.selectList(queryWrapper);
    }

    /**
     * 立即执行子任务
     * 单次任务执行完成之后不会创建下一次执行任务，
     * 多次以及循环任务记录执行次数
     *
     * @param childTaskCode 子任务编码
     */
    @Override
    public void startTask(String childTaskCode) {
        TaskParam taskParam = taskParamMapper.selectTaskByChildTaskCode(childTaskCode);

        Set<Object> objSet = redisService.getZSetList(TaskConstant.TASK_QUEUE, 0, -1);

        for (Object obj : objSet) {
            TaskEntity taskEntity = (TaskEntity) obj;
            if (taskEntity.getChildTaskCode().equals(taskParam.getChildTaskCode())) {
                redisService.removeZSetByMember(TaskConstant.TASK_QUEUE, obj);
                redisService.setZSet(TaskConstant.TASK_QUEUE, taskEntity, 0);
            }
        }
    }
}
