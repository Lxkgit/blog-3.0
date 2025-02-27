package com.blog.core.entity.auth;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Integer id;

    //角色编码
    private String roleCode;

    //角色名称
    private String roleName;
}
