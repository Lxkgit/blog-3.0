package com.blog.core.domain.file.task.vo;

import com.blog.core.domain.file.task.entity.TaskLog;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TaskLogVo extends TaskLog {

    private Integer pageNum;

    private Integer pageSize;

    /**
     * 当前任务流水号下日志数量
     */
    private Integer logCount;
}
