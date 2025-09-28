package com.blog.core.domain.file.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @Description 任务日志表
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@Data
public class TaskLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 任务名称
     */
    private String taskName;

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
    @TableField(value = "task_uuid")
    private String taskUUID;

    /**
     * 任务日志类型
     * 1 发起任务日志
     * 2 响应任务日志
     */
    private Integer taskLogType;

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
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;


}
