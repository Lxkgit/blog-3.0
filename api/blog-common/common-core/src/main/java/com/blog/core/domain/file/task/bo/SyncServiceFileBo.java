package com.blog.core.domain.file.task.bo;

import lombok.Data;

import java.util.List;

/**
 * @Description 服务器云盘文件同步
 * @Author lxk
 * @CreateTime 2025-10-28
 */

@Data
public class SyncServiceFileBo {

    /**
     * 同步任务所属用户
     */
    private Integer userId;

    /**
     * minio中文件存储路径
     */
    private String minioPath;

    /**
     * 树莓派中文件存储绝对路径
     */
    private String devicePath;

    /**
     * 单次任务同步文件数量
     */
    private List<String> fileNameList;

    /**
     * 同步目录下最大待处理文件数量
     */
    private Integer maxFileCount;

}
