package com.blog.file.service;

import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.task.domain.TaskEntity;

import java.util.List;

/**
 * @author lxk
 * @description 任务服务
 * @date 2025/08/22
 */

public interface TaskService {

    void updateTask(TaskEntity taskInfoVo);

    List<Object> selectTaskInfoList();

    List<TaskLog> selectTaskLogList(TaskLogVo taskLogVo);
}
