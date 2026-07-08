package com.blog.core.domain.file.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-29
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TaskParam {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
     * 主任务编码
     */
    private String taskName;

    /**
     * json格式任务执行参数
     */
    private String paramJson;

    /**
     * 任务执行次数
     */
    private Integer taskCount;

    /**
     * 任务触发方式：1：指定时间 2：延时 3： cron表达式
     */
    private String taskTrigger;

    /**
     * 任务启动时间（延时方式记录为秒）
     */
    private String taskTime;

    /**
     * 任务状态 1：启用 2：禁用 3： 执行完成
     */
    private String taskStatus;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 最近修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
