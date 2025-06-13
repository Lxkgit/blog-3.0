package com.blog.pi.socket.domain.dto;

import lombok.Data;

import java.util.List;

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
     * 文件最终上传服务器位置
     */
    private String servicePath;

    /**
     * 文件移动数量
     */
    private Integer count;

    /**
     * 文件名称列表
     */
    private List<String> fileNameList;

}
