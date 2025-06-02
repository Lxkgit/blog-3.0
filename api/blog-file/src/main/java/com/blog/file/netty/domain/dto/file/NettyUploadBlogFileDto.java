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
    private String result;

    private String filePath;

    private List<String> fileNameList;

}
