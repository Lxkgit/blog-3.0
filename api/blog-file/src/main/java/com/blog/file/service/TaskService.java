package com.blog.file.service;

import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.core.result.ResultPage;
import com.blog.task.domain.TaskEntity;

import java.util.List;

/**
 * @author lxk
 * @description 任务服务
 * @date 2025/08/22
 */

public interface TaskService {

    void updateTask(TaskParamVo taskParamVo);

    List<Object> selectTaskBaseList();

    List<TaskParam> selectTaskEntityById(String taskCode);

    ResultPage<TaskLogVo> selectTaskLogList(TaskLogVo taskLogVo);

    void createChildTask(TaskEntity taskEntity, TaskParam taskParam);

    void startTask(String childTaskCode);
}
