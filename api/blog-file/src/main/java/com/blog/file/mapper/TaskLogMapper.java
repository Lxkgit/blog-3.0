package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.task.entity.TaskLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 任务日志类
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@Mapper
public interface TaskLogMapper extends BaseMapper<TaskLog> {
}
