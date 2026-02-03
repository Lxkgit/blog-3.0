package com.blog.task.mapper;

import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lxk
 * @description 任务参数
 * @date 2025/09/16
 */

@Mapper
public interface TaskParamMapper {

    void updateTaskParamById(TaskParamVo taskParamVo);

    List<TaskParam> selectTaskByTaskCode(TaskParamVo taskParamVo);

    TaskParam selectTaskByChildTaskCode(String childTaskCode);

    TaskParam selectTaskParamById(@Param("id") Integer id);
}
