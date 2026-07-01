package com.blog.core.domain.file.task.vo;

import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.valication.group.SelectListGroup;
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
}
