package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author lxk
 * @description 任务参数
 * @date 2025/09/16
 */

@Mapper
public interface TaskParamMapper extends BaseMapper<TaskParam> {

    List<TaskParam> selectTaskByTaskUUID(TaskParamVo taskParamVo);
}
