package com.blog.core.domain.socket.dto;

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
     *
     */
    private String servicePath;

    /**
     * 文件移动数量
     */
    private Integer count;




    /**
     * 上下文类型: 用于文件移动操作，识别上下文类型，执行对应方法流程
     * 1: 简单的文件或目录移动，无上下文流程
     * 2: 树莓派文件上传流程 updateBlogFileSecondStep
     */
    private Integer type;

    /**
     * 文件移动携带上下文信息
     */
    private String data;

//    /**
//     * 文件移动源路径
//     */
//    private String sourceDirectory;

//    /**
//     * 文件移动目标路径
//     */
//    private String targetDirectory;

//    /**
//     * 文件最终上传服务器位置
//     */
//    private String servicePath;

    /**
     * 文件来源
     * 1 系统内文件
     * 2 系统外文件
     */
    private Integer fileSource;

//    /**
//     * 文件移动数量
//     */
//    private Integer count;

    /**
     * 文件名称列表
     */
    private List<String> fileNameList;

    /**
     * 文件移动后是否删除源文件所属目录
     * 0: 否
     * 1: 是
     */
    private Integer dirDeleteFlag;
}
