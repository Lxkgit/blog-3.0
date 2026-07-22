package com.blog.core.domain.file.task.vo;

import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.valication.annotation.Equal;
import com.blog.core.valication.group.InsertGroup;
import com.blog.core.valication.group.SelectListGroup;
import com.blog.core.valication.group.UpdateGroup;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-29
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TaskParamVo extends TaskParam {

    /**
     * 页大小
     */
    @NotNull(message = "分页查询页大小不能为空", groups = {SelectListGroup.class})
    @Max(value = 100, message = "分页大小最大为100", groups = {SelectListGroup.class})
    @Min(value = 5, message = "分页大小最小为5", groups = {SelectListGroup.class})
    private Integer pageSize;

    /**
     * 页数
     */
    @NotNull(message = "分页查询页数不能为空", groups = {SelectListGroup.class})
    @Min(value = 1, message = "分页查询页数最小1", groups = {SelectListGroup.class})
    private Integer pageNum;

    /**
     * 任务执行次数
     */
    @NotNull(message = "任务执行次数不能为空", groups = {InsertGroup.class, UpdateGroup.class})
    @Min(value = -1, message = "任务执行次数最小为-1", groups = {InsertGroup.class, UpdateGroup.class})
    private Integer taskCount;

    /**
     * 任务触发方式：1：指定时间 2：延时 3：cron表达式
     */
    @NotNull(message = "任务触发方式不能为空", groups = {InsertGroup.class, UpdateGroup.class})
    @Equal(value = "1,2,3", message = "任务触发方式：1：指定时间 2：延时 3： cron表达式 字段错误", groups = {InsertGroup.class, UpdateGroup.class})
    private String taskTrigger;

    /**
     * 任务状态 1：启用 2：禁用 3：执行完成
     */
    @NotNull(message = "任务状态不能为空", groups = {InsertGroup.class, UpdateGroup.class})
    @Equal(value = "1,2,3", message = "任务状态 1：启用 2：禁用 3：执行完成 字段错误", groups = {InsertGroup.class, UpdateGroup.class})
    private String taskStatus;
}
