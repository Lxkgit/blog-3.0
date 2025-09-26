package com.blog.pi.netty.dto.file;

import lombok.Data;

import java.util.List;

@Data
public class NettyFileSyncDto {

    /**
     * syncResult 文件同步状态
     * 0 请求已收到
     * 1 下载完成
     * 2 上传完成
     */
    private Integer syncResult;

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
