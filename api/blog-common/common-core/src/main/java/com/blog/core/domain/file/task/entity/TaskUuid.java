package com.blog.core.domain.file.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @Description 任务 uuid 映射表
 * @Author lxk
 * @CreateTime 2026-07-01
 */

@Data
public class TaskUuid {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * task_param 表id
     */
    private Integer taskId;

    /**
     * 任务uuid
     */
    private String uuid;
}
