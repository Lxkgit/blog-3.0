package com.blog.file.socket.domain.dto;

import lombok.Data;

/**
 *
 */
@Data
public class MoveFileDto {

    /**
     * netty 消息id
     */
    private String requestId;

    /**
     * 文件移动源路径
     */
    private String sourceDirectory;

    /**
     * 文件移动目标路径
     */
    private String targetDirectory;

    /**
     *
     */
    private String servicePath;

    /**
     * 文件移动数量
     */
    private Integer count;

}
