package com.blog.file.netty.domain.dto.file;

import lombok.Data;

import java.util.List;

@Data
public class NettyUploadBlogFileDto {

    /**
     * 设备上传文件目录
     */
    private String deviceFilePath;

    /**
     * 设备上传文件至服务器目录
     */
    private String serviceFilePath;

    /**
     * 上传文件数量
     */
    private Integer count;

    //  文件定时同步响应字段
    /**
     * syncResult 文件同步状态
     * 0 请求已收到
     * 1 下载完成
     * 2 上传完成
     */
    private Integer syncResult;

    private String minioPath;

    private List<String> fileNameList;

}
