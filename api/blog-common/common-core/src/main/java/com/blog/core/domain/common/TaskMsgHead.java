package com.blog.core.domain.common;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-09-19
 */

@Data
@SuperBuilder
public class TaskMsgHead {

    /**
     * 任务步骤id (用于跨方法、跨平台记录任务步骤执行时间)
     */
    private Integer logStepId;

    /**
     * 任务所属用户id
     */
    private Integer userId;

    /**
     * 基础任务唯一编码
     */
    private String taskCode;

    /**
     * 当前任务执行编码
     */
    private String taskUuid;



    /**
     * 子任务唯一编码
     */
    private String childTaskCode;

    /**
     * 指定子任务任务执行流水号
     */
    private String subTaskUuid;

    /**
     * 任务参数 （JSON格式）
     */
    private String taskParam;
}
