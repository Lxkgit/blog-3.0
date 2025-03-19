package com.blog.core.domain.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("sys_menu")
public class Menu {

    @TableId(type = IdType.AUTO)
    private Integer id;

    //父级id
    private Integer parentId;

    //菜单名称/按钮名称
    private String menuName;

    // 前端vue 跳转路径
    private String menuPath;

    //权限名称
    private String auth;

    //类型  1:菜单 , 2:按钮
    private String isType;

    //排序序号
    private Integer sort;

    //创建日期
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
