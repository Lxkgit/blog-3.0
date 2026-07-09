package com.blog.file.service;


import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.core.result.ResultPage;
import com.blog.timer.action.TimerAction;
import com.blog.timer.entity.TimerTask;

import java.util.List;
import java.util.Map;

/**
 * @author lxk
 * @description 任务服务
 * @date 2025/08/22
 */

public interface TaskService {

    void insertTask(TaskParamVo taskParamVo);

    void deleteTask(Integer id);

    void updateTask(TaskParamVo taskParamVo);

    ResultPage<TaskParam> selectTaskList(TaskParamVo taskParamVo);

    void createTask(TaskParamVo taskParamVo);

    void startTask(Integer id);

    void stopTask(Integer id);

    void runningTask(Integer id);

    void cancelTask(Integer id);

    List<TimerAction> selectBaseTaskList();

    ResultPage<Map<String, Object>> selectRunningTask(TaskParamVo taskParamVo);

}
