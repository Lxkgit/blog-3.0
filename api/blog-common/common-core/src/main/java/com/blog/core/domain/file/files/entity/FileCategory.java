package com.blog.core.domain.file.files.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description 文件目录表
 * @Author lxk
 * @CreateTime 2025-04-13
 */

@Data
@TableName("file_category")
public class FileCategory {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 目录名称
     */
    private String dirName;

    /**
     * 目录相对路径
     */
    private String dirPath;

    /**
     * 父目录id
     */
    private Integer parentDir;

    /**
     * 目录所属用户
     */
    private Integer userId;

    /**
     * 共享用户id列表
     */
    private String shareUser;

    /**
     * 本地文件总数
     */
    private Integer localFileCount;

    /**
     * 远程服务器文件总数
     */
    private Integer remoteFileCount;

    /**
     * 目录占用空间大小
     */
    private Integer occupySpace;

    /**
     * 创建用户
     */
    private String createBy;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
