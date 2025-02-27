package com.blog.core.entity.auth;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
