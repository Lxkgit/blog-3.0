package com.blog.core.domain.file.files.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @description: 文件目录下文件数据记录表
 * @Author: lxk
 * @date 2023/8/2 16:07
 */

@Data
@TableName("file_category_data")
public class FileCategoryData {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 文件/目录所属用户
     */
    private Integer userId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件目录id
     */
    private Integer fileCategoryId;

    /**
     * 文件请求地址
     */
    private String fileUrl;

    /**
     * 文件大小（kb）
     */
    private Long fileSize;

    /**
     * 同步文件位置状态 0:本地服务器 1:正在同步本地服务器 2:等待同步 3:正在同步远程服务器 4:远程服务器
     */
    private Integer fileStatus;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件详细信息
     */
    private String fileJson;

    /**
     * 同步文件唯一编码
     */
    private String fileCode;

    /**
     * 创建用户
     */
    private String createBy;

    /**
     * 文件创建时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
