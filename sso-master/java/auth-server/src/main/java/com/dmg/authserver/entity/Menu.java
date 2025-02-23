package com.dmg.authserver.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@TableName("sys_menu")
public class Menu {

    @TableId(type = IdType.AUTO)
    private Integer id;

    //菜单名称/按钮名称
    private String name;

    //权限名称
    private String auth;

    //创建日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    //父级id
    private Integer parentId;

    //类型  1:菜单 , 2:按钮
    private String isType;

    //排序序号
    private Integer paiXu;

    /**
     * 前端vue 跳转路径
     */
    private String path;

    //子集菜单
    @TableField(exist = false)
    private List<Menu> children;
}
