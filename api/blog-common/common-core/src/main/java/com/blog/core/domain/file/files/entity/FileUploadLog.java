package com.blog.core.domain.file.files.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @Author: lxk
 * @date 2022/7/7 16:10
 * @description: 文件上传记录表
 */

@Data
@TableName("file_upload_log")
public class FileUploadLog {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 上传用户id
     */
    private Integer userId;

    /**
     * 上传文件名称
     */
    private String fileName;

    /**
     * 文件地址
     */
    private String fileUrl;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件上传状态 0：正在上传 1：上传成功 2：上传失败
     */
    private Integer uploadState;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 上传用户
     */
    private String createBy;

    /**
     * 上传时间
     */
    private Date createTime;

    public FileUploadLog() {
    }

    /**
     *
     * @param userId 用户id
     * @param fileName 文件名称
     * @param fileType 文件类型
     * @param uploadState 文件上传状态
     * @param createBy 创建人
     * @param createTime 创建时间
     */
    public FileUploadLog(Integer userId, String fileName, String fileType, Integer uploadState, String createBy, Date createTime) {
        this.userId = userId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.uploadState = uploadState;
        this.createBy = createBy;
        this.createTime = createTime;
    }
}
