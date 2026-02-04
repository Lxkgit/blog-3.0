package com.blog.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.vo.TaskLogVo;
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
