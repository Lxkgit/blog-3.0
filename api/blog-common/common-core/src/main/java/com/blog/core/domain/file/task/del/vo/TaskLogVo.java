package com.blog.core.domain.file.task.del.vo;

import com.blog.core.domain.file.task.del.entity.TaskLog;
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

    /**
     * 当前任务流水号下日志数量
     */
    private Integer logCount;
}
