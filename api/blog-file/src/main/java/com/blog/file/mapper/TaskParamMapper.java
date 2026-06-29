package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.task.entity.TaskParam;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lxk
 * @description 定时任务参数mapper
 * @date 2026/06/29
 */

@Mapper
public interface TaskParamMapper extends BaseMapper<TaskParam> {

}
