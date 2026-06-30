package com.blog.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.file.task.del.entity.TaskLog;
import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.core.result.ResultPage;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.TaskParamMapper;
import com.blog.file.service.TaskService;
import com.blog.timer.action.TimerAction;
import com.blog.timer.action.TimerActionManager;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTaskDefinition;
import com.blog.timer.entity.policy.Policy;
import com.blog.timer.entity.trigger.CronTrigger;
import com.blog.timer.manager.TimerManager;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

/**
 * @Description 任务服务实现类
 * @Author lxk
 * @CreateTime 2025-08-22
 */


@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Resource
    private TaskParamMapper taskParamMapper;

    @Resource
    private TimerManager timerManager;

    @Resource
    private TimerActionManager timerActionManager;

    @Override
    public void insertTask(TaskParamVo taskParamVo) {

        // 校验参数
        timerActionManager.get(taskParamVo.getTaskCode()).checkParam(taskParamVo.getParamJson());

        // 保存任务
        taskParamVo.setUserId(SecurityUtil.getLoginUser().getId());
        taskParamVo.setCreateTime(new Date());
        taskParamVo.setUpdateTime(new Date());
        taskParamMapper.insert(taskParamVo);

        // 任务为启动状态时创建任务
        if (Constant.START.equals(taskParamVo.getTaskStatus())) {
            TimerTaskContext context = new TimerTaskContext();
            context.put("param", taskParamVo.getParamJson());
            TimerTaskDefinition definition = TimerTaskDefinition.builder()
                    .taskCode(taskParamVo.getTaskCode())
                    .action(timerActionManager.get(taskParamVo.getTaskCode()))
                    .context(context)
                    .policy(new Policy(taskParamVo.getTaskCount()))
                    .trigger(new CronTrigger("0 50 * * * *"))
                    .build();
            timerManager.schedule(definition);
        }
    }

    @Override
    public void deleteTask(Integer id) {
        taskParamMapper.deleteById(id);


    }

    @Override
    public void updateTask(TaskParamVo taskParamVo) {
        taskParamMapper.updateById(taskParamVo);
    }

    @Override
    public List<TaskParam> selectTaskList(TaskParamVo taskParamVo) {
        LambdaQueryWrapper<TaskParam> wrapper = new LambdaQueryWrapper<>();
        return taskParamMapper.selectList(wrapper);
    }

    @Override
    public List<Object> selectTaskBaseList() {
        return Collections.singletonList(timerActionManager.getAllActions().values());
    }

    @Override
    public List<TaskParam> selectTaskEntityById(String taskCode) {
        return List.of();
    }

//    @Override
//    public ResultPage<TaskLogVo> selectTaskLogList(TaskLogVo taskLogVo) {
//        return null;
//    }

    @Override
    public List<TaskLog> selectTaskLogByTaskUUID(String taskUUID) {
        return List.of();
    }

    @Override
    public void createChildTask(TaskParam taskParam) {

    }

    @Override
    public void startTask(String childTaskCode) {

    }
}
