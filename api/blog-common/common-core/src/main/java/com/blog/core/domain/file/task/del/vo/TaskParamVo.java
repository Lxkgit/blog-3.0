package com.blog.core.domain.file.task.del.vo;

import com.blog.core.domain.file.task.del.entity.TaskParam;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-09-17
 */

@Getter
@Setter
public class TaskParamVo extends TaskParam {

    private List<Integer> taskStatusList;
}
