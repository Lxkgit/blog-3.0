package com.blog.pi.socket;

import com.alibaba.fastjson2.JSON;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 通用WebSocket处理器
 * @Author: lxk
 */
@Component
@ServerEndpoint("/socket/{type}/{id}")
public class SocketService {

    private static final Logger logger = LoggerFactory.getLogger(SocketService.class);

    // 存储所有连接（按类型分组）
    private static final Map<String, Map<String, Session>> connections = new ConcurrentHashMap<>();
    // 存储连接类型与对应数量
    private static final Map<String, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();

    // 会话管理器（通过SpringUtils获取）
    private static ApplicationEventPublisher eventPublisher;

    // 静态注入 ApplicationEventPublisher
    @Autowired
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        SocketService.eventPublisher = eventPublisher;
    }

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

        // 初始化该类型的连接组
        connections.computeIfAbsent(type, k -> new ConcurrentHashMap<>());
        connectionCounts.computeIfAbsent(type, k -> new AtomicInteger(0));

        // 添加到连接池
        connections.get(type).put(id, session);
        int count = connectionCounts.get(type).incrementAndGet();

        logger.info("socket连接 type:{} id:{} 加入，当前{}连接数: {}", type, id, type, count);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        if (connectionType != null && connectionId != null) {
            Map<String, Session> typeConnections = connections.get(connectionType);
            if (typeConnections != null) {
                typeConnections.remove(connectionId);
                int count = connectionCounts.get(connectionType).decrementAndGet();
                logger.info("socket连接类型 type:{} id:{} 退出，当前{}连接数: {}", connectionType, connectionId, connectionType, count);
            }
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        eventPublisher.publishEvent(new SocketReceiveMessage(connectionType, connectionId, session, message));
    }

    @OnError
    public void onError(Throwable error) {
        logger.error("{}连接[ID:{}]发生错误: {}", connectionType, connectionId, error.getMessage(), error);
    }

    /**
     * 发送消息给指定客户端
     */
    public <T> void sendMessage(String type, String id, SocketSendMessage<T> message) {
        Session session = connections.get(type).get(id);
        if (session != null && session.isOpen()) {
            String jsonMessage = JSON.toJSONString(message);
            sendMessageToSession(session, jsonMessage);
            logger.debug("向{}/{}发送消息: {}", type, id, jsonMessage);
        } else {
            logger.warn("目标会话不存在或已关闭: {}/{}", type, id);
        }

    }

    private void sendMessageToSession(Session session, String message) {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    logger.error("消息发送失败: {}", e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 获取当前类型的连接数
     */
    public static int getConnectionCount(String type) {
        AtomicInteger count = connectionCounts.get(type);
        return count != null ? count.get() : 0;
    }
}
