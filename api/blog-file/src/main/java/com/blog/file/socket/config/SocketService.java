package com.blog.file.socket.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.blog.core.exception.ServiceException;
import com.blog.core.domain.socket.SocketPacket;
import com.blog.core.domain.socket.SocketPacketEvent;
import jakarta.websocket.Session;
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
    private static final Map<String, Map<String, Session>> CONNECTIONS = new ConcurrentHashMap<>();

    // 存储连接类型与对应数量
    private static final Map<String, AtomicInteger> CONNECTION_COUNTS = new ConcurrentHashMap<>();

    // 事件发布器
    private static ApplicationEventPublisher eventPublisher;

    @Autowired
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        SocketService.eventPublisher = eventPublisher;
    }

    public Integer socketOpen(Session session, String type, String id) {
        Map<String, Session> typeConnections = CONNECTIONS.computeIfAbsent(type, key -> new ConcurrentHashMap<>());

        AtomicInteger count = CONNECTION_COUNTS.computeIfAbsent(type, key -> new AtomicInteger(0));

        // 注册新会话，获取同 ID 的旧会话
        Session oldSession = typeConnections.put(id, session);

        if (oldSession == null) {
            // 新增一个连接
            count.incrementAndGet();
        } else if (oldSession != session) {
            // 同一个 ID 被新会话替换，不增加连接数
            logger.info("socket连接替换 type:{} id:{} oldSessionId:{} newSessionId:{}", type, id, oldSession.getId(), session.getId());

            // 关闭旧会话，其关闭回调不会删除新会话
            if (oldSession.isOpen()) {
                try {
                    oldSession.close();
                } catch (IOException e) {
                    logger.warn("关闭旧 socket 会话失败 type:{} id:{} sessionId:{}", type, id, oldSession.getId(), e);
                }
            }
        }

        int currentCount = typeConnections.size();

        logger.info("socket连接注册 type:{} id:{} sessionId:{} 当前连接数:{}", type, id, session.getId(), currentCount);

        return currentCount;
    }


    /**
     * WebSocket 连接关闭
     *
     * @param connectionType 客户端类型
     * @param connectionId   客户端连接 ID
     * @param session        实际关闭的 WebSocket 会话
     */
    public void socketClose(String connectionType, String connectionId, Session session) {
        if (connectionType == null || connectionId == null || session == null) {
            return;
        }

        Map<String, Session> typeConnections = CONNECTIONS.get(connectionType);

        if (typeConnections == null) {
            logger.debug("socket连接组不存在，无需清理 type:{} id:{} sessionId:{}", connectionType, connectionId, session.getId());
            return;
        }

        // 只删除 ID 对应的实际会话，防止旧连接关闭时误删新连接
        boolean removed = typeConnections.remove(connectionId, session);

        if (!removed) {
            logger.debug("socket连接未删除，当前连接可能已被替换 type:{} id:{} sessionId:{}", connectionType, connectionId, session.getId());
            return;
        }

        // 仅成功删除连接时才递减计数
        AtomicInteger count = CONNECTION_COUNTS.get(connectionType);
        int currentCount;

        if (count != null) {
            currentCount = count.updateAndGet(value -> Math.max(0, value - 1));
        } else {
            currentCount = typeConnections.size();
        }

        logger.info("socket连接关闭 type:{} id:{} sessionId:{} 当前连接数:{}", connectionType, connectionId, session.getId(), currentCount);

        // 清理空连接组，使用条件删除，避免并发情况下误删新建的连接组
        if (typeConnections.isEmpty()) {
            CONNECTIONS.remove(connectionType, typeConnections);
            CONNECTION_COUNTS.remove(connectionType, count);
        }
    }


    /**
     * 发送消息给指定客户端
     *
     * @param type    客户端类型
     * @param id      客户端连接id
     * @param message 消息内容
     */
    public <T> boolean sendMessage(String type, String id, SocketPacket<T> message) throws ServiceException {

        Map<String, Session> typeConnections = CONNECTIONS.get(type);

        if (typeConnections == null) {
            logger.error("socket连接类型不存在 type:{} id:{} 当前所有类型:{}", type, id, CONNECTIONS.keySet());

            throw new ServiceException("目标会话不存在: " + type + "/" + id);
        }

        Session session = typeConnections.get(id);

        if (session == null) {
            logger.error("socket目标会话不存在 type:{} id:{} 当前该类型的连接ID:{}", type, id, typeConnections.keySet());

            throw new ServiceException("目标会话不存在: " + type + "/" + id);
        }

        if (!session.isOpen()) {
            logger.warn("socket目标会话已关闭 type:{} id:{} sessionId:{}", type, id, session.getId());

            // 只清理当前已经关闭的会话
            typeConnections.remove(id, session);

            AtomicInteger count = CONNECTION_COUNTS.get(type);

            if (count != null) {
                count.updateAndGet(value -> Math.max(0, value - 1));
            }

            if (typeConnections.isEmpty()) {
                CONNECTIONS.remove(type, typeConnections);
                CONNECTION_COUNTS.remove(type);
            }

            throw new ServiceException("目标会话已关闭: " + type + "/" + id);
        }

        String jsonMessage = JSON.toJSONString(message);

        logger.debug("socket发送消息 type:{} id:{} sessionId:{} requestId:{} message:{}", type, id, session.getId(), message.getRequestId(), jsonMessage);

        return sendMessageToSession(session, jsonMessage);
    }

    /**
     * 接收socket消息
     */
    public void receiveMessage(String connectionType, String connectionId, String message, Session session) {
        SocketPacket<Object> socketPacket = JSONObject.parseObject(message, new TypeReference<SocketPacket<Object>>() {
        }.getType());

        // 发布自定义socket数据包处理事件
        eventPublisher.publishEvent(new SocketPacketEvent(connectionType, connectionId, session, socketPacket));
    }

    /**
     * socket消息发送方法
     */
    private boolean sendMessageToSession(Session session, String message) {
        if (session != null && session.isOpen()) {
            synchronized (session) {
                try {
                    if (!session.isOpen()) {
                        return false;
                    }

                    session.getBasicRemote().sendText(message);
                    return true;
                } catch (IOException e) {
                    logger.error("socket消息发送失败 sessionId:{} error:{}", session.getId(), e.getMessage(), e);
                }
            }
        }

        return false;
    }

    /**
     * 获取当前类型的连接数
     */
    public static int getConnectionCount(String type) {
        AtomicInteger count = CONNECTION_COUNTS.get(type);
        return count != null ? count.get() : 0;
    }
}
