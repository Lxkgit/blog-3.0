package com.blog.core.domain.socket.dto;

import lombok.Data;

/**
 * @Description socket 删除文件或目录消息类
 * @Author lxk
 * @CreateTime 2025-06-26
 */

@Data
public class SocketDeleteFileOrDirDto {

    private String dirPath;

    private String fileName;

    private Boolean result;
}
