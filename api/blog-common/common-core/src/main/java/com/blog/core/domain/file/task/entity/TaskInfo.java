package com.blog.core.domain.file.task.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Description 任务详情表
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@Data
public class TaskInfo {

    private Integer id;

    private String taskName;

    /**
     * 任务唯一编码
     */
    private String taskUUID;

    /**
     * 存放任务执行的类、方法、参数
     */
    private String taskJson;

    /**
     * cron 表达式
     */
    private String taskCron;

    /**
     * 任务启动时间，秒不支持小数
     * 1.5h 1.5小时后执行
     * 3m   3分钟后执行
     * 50s  50秒后执行
     */
    private String taskTime;

    /**
     * 任务执行次数
     * -1 为无限执行
     */
    private Integer taskCount;

    /**
     * 任务执行状态 1：执行中 2：已完成
     */
    private Integer taskStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最近修改时间
     */
    private Date updateTime;


}
