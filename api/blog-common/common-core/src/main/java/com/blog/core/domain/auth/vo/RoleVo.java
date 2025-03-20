package com.blog.core.domain.auth.vo;

import com.blog.core.domain.auth.entity.Role;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description 角色数据前端交互类
 * @Author lxk
 * @CreateTime 2025-03-20
 */

@Getter
@Setter
public class RoleVo extends Role {


    private Integer pageNum;

    private Integer pageSize;
}
