package com.blog.core.domain.file.files.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Description 文件同步日志实体类
 * @Author lxk
 * @CreateTime 2025-06-20
 */

@Data
@TableName("file_sync_log")
public class FileSyncLog {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * userId
     */
    private Integer userId;

    /**
     * 文件id
     */
    private Integer fileId;

    /**
     * 消息请求来源 1服务端请求 2设备请求
     */
    private Integer requestSource;

    /**
     * 文件同步类型 1：文件由服务器同步到设备 2：文件由设备上传到服务器
     */
    private Integer syncType;

    /**
     * netty消息唯一序列号
     */
    private String requestId;

    /**
     * json格式netty消息
     */
    private String requestJson;

    /**
     * 服务器文件目录
     */
    private String serviceFilePath;

    /**
     * 设备文件存放目录
     */
    private String deviceFilePath;

    /**
     * 文件在minio中存放路径
     */
    private String minioPath;

    /**
     * 响应类型 1：客户端/服务端接收到数据 2：客户端数据同步完成
     */
    private Integer resultType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建用户
     */
    private String createBy;
}
