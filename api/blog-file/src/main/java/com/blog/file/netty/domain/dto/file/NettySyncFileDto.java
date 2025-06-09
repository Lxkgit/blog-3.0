package com.blog.file.netty.domain.dto.file;

import lombok.Data;

import java.util.List;

/**
 * @Description netty同步文件类
 * @Author lxk
 * @CreateTime 2025-06-09
 */

@Data
public class NettySyncFileDto {

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 文件编码
     */
    private String fileCode;

    /**
     * 必填
     * 文件同步类型
     * 1：文件由服务器同步到设备
     * 2：文件由设备上传到服务器
     */
    private Integer syncType;

    /**
     * 必填
     * 服务器文件目录
     */
    private String serviceFilePath;

    /**
     * 必填
     * 设备文件存放目录
     */
    private String deviceFilePath;

    /**
     * 文件在minio中存放路径
     * 同步类型为 2 时必填
     */
    private String minioPath;

    /**
     * 同步文件名称
     */
    private List<String> fileNameList;

    /**
     * 同步文件数量
     * 同步类型为 2 时，将设备文件存放目录中获取指定数量文件上传
     */
    private Integer count;

    // -----------数据响应字段----------------

    /**
     * 响应类型 因为文件同步处理时间较长，而netty又需要做消息重发功能，所以文件同步客户端netty会响应两次
     * 1：客户端接收到数据
     * 2：客户端数据同步完成
     */
    private Integer resultType;


}
