package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.task.entity.TaskUuid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author lxk
 * @description
 * @date 2026/07/01
 */

@Mapper
public interface TaskUuidMapper extends BaseMapper<TaskUuid> {

    String selectUUidByTaskId(@Param("taskId") Integer taskId);
}
