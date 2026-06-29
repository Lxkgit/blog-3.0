package com.blog.task.mapper;

import com.blog.core.domain.file.task.del.entity.TaskLog;
import com.blog.core.domain.file.task.del.vo.TaskLogVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description 任务日志类
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@Mapper
public interface TaskLogMapper {

    void insertTaskLog(TaskLog taskLog);

    List<TaskLogVo> selectTaskLogList();

    void updateTaskLogEndTimeByTaskUUID(String taskUUID);

    List<TaskLog> selectTaskLogByUUID(String taskUUID);
}
