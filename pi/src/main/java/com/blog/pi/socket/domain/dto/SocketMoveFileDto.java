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
     * 上下文类型: 用于文件移动操作，识别上下文类型，执行对应方法流程
     * 0：data数据转换为 NettySyncFileDto.class
     */
    private Integer type;

    /**
     * 文件移动携带上下文信息
     */
    private String data;

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
