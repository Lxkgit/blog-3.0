package com.blog.pi.netty.dto.file;

import lombok.Data;

import java.util.List;

/**
 * @Description netty同步文件类
 * @Author lxk
 * @CreateTime 2025-06-09
 */

@Data
public class NettySyncFileDto {

    // -----------消息发送状态字段----------------

    /**
     * 响应类型 因为文件同步处理时间较长，而netty又需要做消息重发功能，所以文件同步客户端netty会响应两次
     * 1：客户端接收到数据
     * 2：客户端数据同步完成
     */
    private Integer resultType;

    /**
     * syncType 文件同步类型
     * 1 下载
     * 2 上传
     */
    private Integer syncType;

    /**
     * 同步结果
     */
    private Boolean syncResult;

    // -----------数据发送字段----------------

    /**
     * 上传/下载-ftp中文件目录
     */
    private String serviceFilePath;

    /**
     * 上传/下载-文件放入设备下目录名称
     */
    private String deviceFilePath;

    /**
     * 上传-不指定名称时上传文件数量
     */
    private Integer count;

    /**
     * 上传-写入minio中路径
     */
    private String minioPath;

    /**
     * 上传-是否删除minio中原文件
     * 0: 删除
     * 1: 保留
     */
    private Integer minioDeleteFlag;

    /**
     * 上传/下载-指定文件名称
     */
    private List<String> fileNameList;

    // -----------数据响应字段----------------

    /**
     * 上传/下载-设备处理失败错误原因
     */
    private String errorMsg;

    public static NettySyncFileDto buildSyncToDevice(String serviceFilePath, String deviceFilePath) {
        NettySyncFileDto nettySyncFileDto = new NettySyncFileDto();
        nettySyncFileDto.setSyncType(1);
        nettySyncFileDto.setServiceFilePath(serviceFilePath);
        nettySyncFileDto.setDeviceFilePath(deviceFilePath);
        return nettySyncFileDto;
    }

    public static NettySyncFileDto buildSyncToService(String minioPath, String servicePath, String deviceFilePath) {
        NettySyncFileDto nettySyncFileDto = new NettySyncFileDto();
        nettySyncFileDto.setSyncType(2);
        nettySyncFileDto.setMinioPath(minioPath);
        nettySyncFileDto.setServiceFilePath(servicePath);
        nettySyncFileDto.setDeviceFilePath(deviceFilePath);
        return nettySyncFileDto;
    }
}
