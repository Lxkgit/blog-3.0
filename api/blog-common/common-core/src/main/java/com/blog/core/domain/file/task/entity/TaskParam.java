package com.blog.core.domain.file.task.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description 任务参数表
 * @Author lxk
 * @CreateTime 2025-09-16
 */

@Data
public class TaskParam {

    private Integer id;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 主任务编码
     */
    private String taskCode;

    /**
     * 子任务编码
     */
    private String childTaskCode;

    /**
     * json格式参数类
     */
    private String paramClazz;

    /**
     * json格式任务执行参数
     */
    private String paramJson;

    /**
     * 任务执行次数
     */
    private Integer taskCount;

    /**
     * 任务执行cron表达式
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
     * 任务状态
     * 1 待执行
     * 2 进行中
     * 3 已结束
     */
    private Integer taskStatus;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 最近修改时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
