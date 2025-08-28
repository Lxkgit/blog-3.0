package com.blog.file.socket.controller;

import com.blog.file.socket.service.SocketService;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description socket服务接口类
 * @Author lxk
 * @CreateTime 2025-08-28
 */

@ServerEndpoint("/socket/{type}/{id}")
public class SocketController {

    private static final Logger logger = LoggerFactory.getLogger(SocketController.class);

    @Resource
    private SocketService socketService;

    // 当前连接的元数据
    private String connectionType;
    private String connectionId;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("type") String type, @PathParam("id") String id) {

        this.connectionType = type;
        this.connectionId = id;

        Integer count = socketService.socketOpen(session, type, id);

        logger.info("socket连接 type:{} id:{} 加入，当前{}连接数: {}", type, id, type, count);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        socketService.socketClose(connectionType, connectionId);
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        socketService.receiveMessage(connectionType, connectionId, message, session);
    }

    @OnError
    public void onError(Throwable error) {
        logger.error("{}连接[ID:{}]发生错误: {}", connectionType, connectionId, error.getMessage(), error);
    }






}
