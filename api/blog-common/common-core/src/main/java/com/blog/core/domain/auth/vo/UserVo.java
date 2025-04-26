package com.blog.core.domain.auth.vo;

import com.blog.core.domain.auth.entity.Menu;
import com.blog.core.domain.auth.entity.Role;
import com.blog.core.domain.auth.entity.User;
import com.blog.core.valication.group.SelectListGroup;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserVo extends User {


    private List<Integer> roleIds;

    /**
     * 用户角色信息
     */
    private List<Role> roleList;

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
