package com.blog.file.service;

import com.blog.core.domain.file.task.vo.TaskInfoVo;
import com.blog.core.domain.file.task.vo.TaskLogVo;

import java.util.List;

/**
 * @author lxk
 * @description 任务服务
 * @date 2025/08/22
 */

public interface TaskService {

    void insertTask(TaskInfoVo taskInfoVo);

    void updateTask(TaskInfoVo taskInfoVo);

    void deleteTask(Integer id);

    List<TaskInfoVo> selectTaskInfoList();

    List<TaskLogVo> selectTaskLogList();
}
