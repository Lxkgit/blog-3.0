package com.blog.core.domain.netty.dto.file;

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
     * 0 失败
     * 1 成功
     */
    private Integer syncResult;

    /**
     * 同步任务是否结束
     * 0 否
     * 1 是
     */
    private Integer syncEnd;

    /**
     * 文件同步次数
     * null、1 单次同步
     * 2 多次同步
     */
    private Integer syncCount;

    /**
     * 操作前是否需要校验文件已存在
     * 0 否
     * 1 是
     */
    private Integer checkFile;

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
     * 下载-是否删除minio中原文件
     * 0: 否
     * 1: 是
     */
    private Integer minioDeleteFlag;

    /**
     * 文件编码
     * 上传/下载-修改指定文件状态
     * 格式: id:fileName
     */
    private List<String> fileNameList;

//    /**
//     * 文件编码
//     * 上传/下载-修改指定文件状态
//     * 格式: id:fileName
//     */
//    private List<String> fileCodeList;

    /**
     * 文件来源
     * 1 系统内文件 fileNameList 格式: id:fileName
     * 2 系统外文件 fileNameList 格式: fileName
     */
    private Integer fileSource;

    // -----------数据响应字段----------------

    /**
     * 上传/下载-设备处理失败错误原因
     */
    private String errorMsg;

    public static NettySyncFileDto buildSyncToDevice(String serviceFilePath, String deviceFilePath) {
        NettySyncFileDto nettySyncFileDto = new NettySyncFileDto();
        nettySyncFileDto.setFileSource(1);
        nettySyncFileDto.setSyncType(1);
        nettySyncFileDto.setServiceFilePath(serviceFilePath);
        nettySyncFileDto.setDeviceFilePath(deviceFilePath);
        return nettySyncFileDto;
    }

    public static NettySyncFileDto buildSyncToService(String minioPath, String servicePath, String deviceFilePath) {
        NettySyncFileDto nettySyncFileDto = new NettySyncFileDto();
        nettySyncFileDto.setSyncType(2);
        nettySyncFileDto.setFileSource(1);
        nettySyncFileDto.setMinioPath(minioPath);
        nettySyncFileDto.setServiceFilePath(servicePath);
        nettySyncFileDto.setDeviceFilePath(deviceFilePath);
        return nettySyncFileDto;
    }
}
