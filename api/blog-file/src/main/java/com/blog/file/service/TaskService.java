package com.blog.file.service;

import com.blog.core.domain.file.task.del.entity.TaskLog;

import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.core.result.ResultPage;

import java.util.List;

/**
 * @author lxk
 * @description 任务服务
 * @date 2025/08/22
 */

public interface TaskService {

    void insertTask(TaskParamVo taskParamVo);

    void deleteTask(Integer id);

    void updateTask(TaskParamVo taskParamVo);

    List<TaskParam> selectTaskList(TaskParamVo taskParamVo);

    List<Object> selectTaskBaseList();

    List<TaskParam> selectTaskEntityById(String taskCode);

//    ResultPage<TaskLogVo> selectTaskLogList(TaskLogVo taskLogVo);

    List<TaskLog> selectTaskLogByTaskUUID(String taskUUID);

    void createChildTask(TaskParam taskParam);

    void startTask(String childTaskCode);
}
