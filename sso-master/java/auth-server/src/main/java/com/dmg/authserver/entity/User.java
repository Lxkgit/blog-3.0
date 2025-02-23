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
@TableName("sys_user")
public class User {
 
    @TableId(type = IdType.AUTO)
    private Integer id;
 
    //账号
    private String account;
 
    //密码
    private String password;
 
    //姓名
    private String name;

    //创建日期
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(exist = false)
    private List<Menu> menu;

}