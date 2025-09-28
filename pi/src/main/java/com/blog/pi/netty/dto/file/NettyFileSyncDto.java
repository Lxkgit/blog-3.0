package com.blog.pi.netty.dto.file;

import lombok.Data;

import java.util.List;

@Data
public class NettyFileSyncDto {

    /**
     * syncType 文件同步类型
     * 0 请求已收到
     * 1 下载
     * 2 上传
     */
    private Integer syncType;

    /**
     * 同步结果
     */
    private Boolean syncResult;

    /**
     * 文件名称列表
     */
    private List<String> fileNameList;

    /**
     * 文件写入minio路径
     */
    private String minioPath;

    /**
     * 设备上传文件至服务器目录
     */
    private String serviceFilePath;



    /**
     * 设备上传文件目录
     */
    private String deviceFilePath;

    /**
     * 上传文件数量
     */
    private Integer count;
}
