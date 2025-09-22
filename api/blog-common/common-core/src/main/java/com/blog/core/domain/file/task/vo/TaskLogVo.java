package com.blog.core.domain.file.task.vo;

import com.blog.core.domain.file.task.entity.TaskLog;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@Getter
@Setter
public class TaskLogVo extends TaskLog {

    private Integer pageNum;

    private Integer pageSize;

    private Integer logCount;
}
