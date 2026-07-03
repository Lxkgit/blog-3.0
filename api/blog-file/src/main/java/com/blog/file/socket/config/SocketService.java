package com.blog.file.socket.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.blog.file.socket.domain.SocketPacket;
import com.blog.file.socket.domain.SocketPacketEvent;
import jakarta.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 通用WebSocket处理器
 * @Author: lxk
 */

@Service
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

    public Integer socketOpen(Session session, String type, String id) {
        // 初始化该类型的连接组
        connections.computeIfAbsent(type, k -> new ConcurrentHashMap<>());
        connectionCounts.computeIfAbsent(type, k -> new AtomicInteger(0));

        // 添加到连接池
        connections.get(type).put(id, session);
        return connectionCounts.get(type).incrementAndGet();
    }

    public void socketClose(String connectionType, String connectionId) {
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
     * 发送消息给指定客户端
     * @param type 客户端类型
     * @param id 客户端连接id
     * @param message 消息内容
     * @param <T>
     */
    public <T> boolean sendMessage(String type, String id, SocketPacket<T> message) {
        Session session = connections.get(type).get(id);
        if (session != null && session.isOpen()) {
            String jsonMessage = JSON.toJSONString(message);
            logger.debug("socket 发送消息: 向{}/{}发送消息: requestId: {} message: {}", type, id, message.getRequestId(), jsonMessage);
            return sendMessageToSession(session, jsonMessage);
        } else {
            logger.warn("目标会话不存在或已关闭: {}/{}", type, id);
            return false;
        }
    }

    /**
     * 接收socket消息
     *
     * @param connectionType socket客户端类型
     * @param connectionId 客户端id
     * @param message 消息内容
     * @param session 连接会话
     */
    public void receiveMessage(String connectionType, String connectionId, String message, Session session) {
        // 报文解析处理
        // 处理泛型：new TypeReference<SocketPacket<Object>>() {}.getType()
        // TypeReference：解决Java泛型类型擦除问题，保留SocketPacket<Object>的类型信息，确保反序列化时能正确识别泛型类型
        // SocketPacket：自定义的泛型类，可能用于封装网络传输的数据包，Object表示其携带的数据类型可以是任意对象
        SocketPacket<Object> socketPacket = JSONObject.parseObject(message, new TypeReference<SocketPacket<Object>>() {
        }.getType());
        // 发布自定义 socket 数据包处理事件
        eventPublisher.publishEvent(new SocketPacketEvent(connectionType, connectionId, session, socketPacket));
    }

    /**
     * socket 消息发送方法
     *
     * @param session
     * @param message
     */
    private boolean sendMessageToSession(Session session, String message) {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                try {
                    session.getBasicRemote().sendText(message);
                    return true;
                } catch (IOException e) {
                    logger.error("消息发送失败: {}", e.getMessage(), e);
                }
            }
        }
        return false;
    }

    /**
     * 获取当前类型的连接数
     */
    public static int getConnectionCount(String type) {
        AtomicInteger count = connectionCounts.get(type);
        return count != null ? count.get() : 0;
    }


}
