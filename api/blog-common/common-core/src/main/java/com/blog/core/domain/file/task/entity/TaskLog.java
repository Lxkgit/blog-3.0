package com.blog.core.domain.file.task.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Description 任务日志表
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@Data
public class TaskLog {

    private Integer id;

    /**
     * 任务唯一编码
     */
    private String taskUUID;

    /**
     * 当前执行次数
     */
    private Integer indexCount;

    /**
     * 任务执行总次数
     * -1 为无限执行
     */
    private Integer taskCount;

    /**
     * 任务执行结果 0失败 1成功
     */
    private Integer taskResultStatus;

    /**
     * 任务执行返回结果
     */
    private String taskResult;

    /**
     * 任务执行异常报错信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    private Date createTime;

}
