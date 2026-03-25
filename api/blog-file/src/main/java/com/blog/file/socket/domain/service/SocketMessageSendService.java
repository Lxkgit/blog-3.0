package com.blog.file.socket.domain.service;

import com.blog.core.domain.common.MsgHead;
import com.blog.file.socket.domain.SocketPacket;
import com.blog.file.socket.domain.constant.SocketClientType;
import com.blog.file.socket.domain.constant.SocketConstant;
import com.blog.file.socket.domain.constant.SocketTopic;
import com.blog.file.socket.domain.dto.SocketDeleteFileOrDirDto;
import com.blog.file.socket.config.SocketService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Description socket 消息发送封装服务
 * @Author lxk
 * @CreateTime 2025-06-26
 */

@Service
public class SocketMessageSendService {

    @Resource
    private SocketService socketService;

    /**
     * 删除指定目录
     *
     * @param dirPath 被删除目录
     */
    public void deleteDir(String dirPath, MsgHead msgHead) {
        SocketDeleteFileOrDirDto dto = new SocketDeleteFileOrDirDto();
        dto.setDirPath(dirPath);
        SocketPacket<SocketDeleteFileOrDirDto> packet = SocketPacket.buildRequest(SocketTopic.SOCKET_DELETE_FILE_OR_DIR, msgHead, dto);
        socketService.sendMessage(SocketClientType.PYTHON, SocketConstant.LOCALHOST_REGISTER_CODE, packet);
    }

    /**
     * 删除指定文件
     *
     * @param dirPath  文件所在目录
     * @param fileName 文件名称
     */
    public void deleteFile(String dirPath, String fileName) {
        SocketDeleteFileOrDirDto dto = new SocketDeleteFileOrDirDto();
        dto.setDirPath(dirPath);
        dto.setFileName(fileName);
        SocketPacket<SocketDeleteFileOrDirDto> packet = SocketPacket.buildRequest(SocketTopic.SOCKET_DELETE_FILE_OR_DIR, dto);
        socketService.sendMessage(SocketClientType.PYTHON, SocketConstant.LOCALHOST_REGISTER_CODE, packet);
    }
}
