package com.blog.file.socket.domain.dto;

import lombok.Data;

/**
 * socket文件移动类
 */
@Data
public class SocketMoveFileDto {

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
