package com.blog.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.ResultPage;
import com.blog.core.result.ResultPageUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.TaskLogMapper;
import com.blog.file.mapper.TaskParamMapper;
import com.blog.file.mapper.TaskUuidMapper;
import com.blog.file.service.TaskService;
import com.blog.timer.action.TimerAction;
import com.blog.timer.action.TimerActionManager;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTask;
import com.blog.timer.entity.TimerTaskDefinition;
import com.blog.timer.entity.policy.Policy;
import com.blog.timer.entity.trigger.AtTimeTrigger;
import com.blog.timer.entity.trigger.CronTrigger;
import com.blog.timer.entity.trigger.DelayTrigger;
import com.blog.timer.entity.trigger.Trigger;
import com.blog.timer.manager.TimerManager;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private TaskLogMapper taskLogMapper;

    @Resource
    private TaskUuidMapper taskUuidMapper;

    @Resource
    private TimerManager timerManager;

    @Resource
    private TimerActionManager timerActionManager;

    @Override
    public void insertTask(TaskParamVo taskParamVo) {

        if (!timerActionManager.checkCode(taskParamVo.getTaskCode())) {
            throw new ServiceException("任务编码错误");
        }

        TimerAction action = timerActionManager.get(taskParamVo.getTaskCode());

        // 校验任务参数
        action.checkParam(action, taskParamVo.getParamJson());

        // 保存任务
        taskParamVo.setUserId(SecurityUtil.getLoginUser().getId());
        taskParamVo.setCreateTime(new Date());
        taskParamVo.setUpdateTime(new Date());
        taskParamMapper.insert(taskParamVo);

        taskParamVo.setUserId(SecurityUtil.getLoginUser().getId());
        createTask(taskParamVo);

    }

    @Override
    public void deleteTask(Integer id) {
        taskParamMapper.deleteById(id);

        String uuid = taskUuidMapper.selectUUidByTaskId(id);
        if (StringUtils.isNotEmpty(uuid)) {
            timerManager.cancel(uuid);
        }
    }

    @Override
    public void updateTask(TaskParamVo taskParamVo) {
        taskParamVo.setUpdateTime(new Date());
        taskParamMapper.updateById(taskParamVo);
        taskParamVo.setUserId(SecurityUtil.getLoginUser().getId());
        createTask(taskParamVo);
    }

    /**
     * 创建定时任务
     */
    @Override
    public void createTask(TaskParamVo taskParamVo) {
        if (!"1".equals(taskParamVo.getTaskStatus())) {
            return;
        }
        cancelTask(taskParamVo.getId());
        // 任务触发方式：1：指定时间 2：延时 3： cron表达式
        Trigger trigger;
        if ("1".equals(taskParamVo.getTaskTrigger())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(taskParamVo.getTaskTime(), formatter);
            trigger = new AtTimeTrigger(localDateTime);
        } else if ("2".equals(taskParamVo.getTaskTrigger())) {
            Duration duration = Duration.ofSeconds(Long.parseLong(taskParamVo.getTaskTime()));
            trigger = new DelayTrigger(duration);
        } else if ("3".equals(taskParamVo.getTaskTrigger())) {
            trigger = new CronTrigger(taskParamVo.getTaskTime());
        } else {
            return;
        }

        // 任务为启动状态时创建任务
        if (Constant.START.equals(taskParamVo.getTaskStatus())) {
            TimerTaskContext context = new TimerTaskContext();
            context.put("param", taskParamVo.getParamJson());
            TimerTaskDefinition definition = TimerTaskDefinition.builder()
                    .id(taskParamVo.getId())
                    .userId(taskParamVo.getUserId())
                    .taskCode(taskParamVo.getTaskCode())
                    .action(timerActionManager.get(taskParamVo.getTaskCode()))
                    .context(context)
                    .policy(new Policy(taskParamVo.getTaskCount()))
                    .trigger(trigger)
                    .build();
            timerManager.schedule(definition);
        }
    }

    @Override
    public ResultPage<TaskParam> selectTaskList(TaskParamVo taskParamVo) {
        PageHelper.startPage(taskParamVo.getPageNum(), taskParamVo.getPageSize());
        LambdaQueryWrapper<TaskParam> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TaskParam::getUpdateTime);
        List<TaskParam> taskLogVoList = taskParamMapper.selectList(wrapper);
        return ResultPageUtils.pageUtil(taskLogVoList, taskParamVo.getPageNum(), taskParamVo.getPageSize(), new PageInfo<>(taskLogVoList).getTotal());
    }

    @Override
    public void startTask(Integer id) {
        TaskParam param = TaskParam.builder().id(id).taskStatus("1").build();
        taskParamMapper.updateById(param);

        TaskParamVo vo = new TaskParamVo();
        BeanUtils.copyProperties(taskParamMapper.selectById(id), vo);
        createTask(vo);
    }

    @Override
    public void stopTask(Integer id) {
        TaskParam param = TaskParam.builder().id(id).taskStatus("2").build();
        taskParamMapper.updateById(param);
    }

    @Override
    public void runningTask(Integer id) {
        String uuid = taskUuidMapper.selectUUidByTaskId(id);
        timerManager.executeNow(uuid);
    }

    @Override
    public void cancelTask(Integer id) {
        String uuid = taskUuidMapper.selectUUidByTaskId(id);
        if (StringUtils.isNotEmpty(uuid)) {
            timerManager.cancel(uuid);
        }
    }

    @Override
    public List<TimerAction> selectBaseTaskList() {
        return timerActionManager.getAllActions().values().stream().toList();
    }

    @Override
    public ResultPage<Map<String, Object>> selectRunningTask(TaskParamVo taskParamVo) {
        List<TimerTask> taskList = timerManager.getAllTask().stream().toList();
        int from = (taskParamVo.getPageNum() - 1) * taskParamVo.getPageSize();
        int to = Math.min(from + taskParamVo.getPageSize(), taskList.size());

        List<Map<String, Object>> resultList = new ArrayList<>();
        List<TimerTask> runList = taskList.subList(from, to);
        for (TimerTask task : runList) {
            // TimerTask 属于依赖包中类 不方便在core包引用
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("timerTask", task);
            objectMap.put("task", taskParamMapper.selectById(task.getDefinition().getId()));
            resultList.add(objectMap);
        }
        return ResultPageUtils.pageUtil(resultList, taskParamVo.getPageNum(), taskParamVo.getPageSize(), taskList.size());
    }


}
