package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.task.entity.TaskInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lxk
 * @description 任务详情类
 * @date 2025/08/22
 */

@Mapper
public interface TaskInfoMapper extends BaseMapper<TaskInfo> {
}
