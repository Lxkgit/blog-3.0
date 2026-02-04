package com.blog.pi.domain.common;

import lombok.Data;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-09-19
 */

@Data
public class TaskMsgHead {

    /**
     * 任务唯一编码
     */
    private String taskCode;

    /**
     * 子任务唯一编码
     */
    private String childTaskCode;

    /**
     * 任务执行流水号
     */
    private String taskUUID;

    /**
     * 指定子任务任务执行流水号
     */
    private String subTaskUUID;
}
