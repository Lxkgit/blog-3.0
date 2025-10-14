package com.blog.core.domain.file.task.bo;

import lombok.Data;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-09-16
 */

@Data
public class SyncDeviceFileBo {

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
    private Integer count;

    /**
     * 同步目录下最大待处理文件数量
     */
    private Integer maxFileCount;
}
