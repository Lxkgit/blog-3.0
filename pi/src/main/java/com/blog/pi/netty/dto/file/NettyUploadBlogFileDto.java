package com.blog.pi.netty.dto.file;

import lombok.Data;

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
}
