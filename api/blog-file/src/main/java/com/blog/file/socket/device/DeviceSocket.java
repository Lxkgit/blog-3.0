package com.blog.file.socket.device;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.file.device.entity.DeviceInfo;
import com.blog.file.mapper.DeviceInfoMapper;
import com.blog.file.socket.SocketMessage;
import com.blog.file.socket.device.domain.constant.DeviceSocketConstant;
import com.blog.file.socket.device.domain.constant.DeviceSocketTopic;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: socket连接服务器py脚本数据监测socket
 * @Author: lxk
 * @date 2023/6/8 10:54
 */
@Component
@ServerEndpoint("/python")
public class DeviceSocket {

    private static final Logger logger = LoggerFactory.getLogger(DeviceSocket.class);

    private static DeviceInfoMapper deviceInfoMapper;

    @Resource
    private void setDeviceInfoMapper(DeviceInfoMapper deviceInfoMapper) {
        DeviceSocket.deviceInfoMapper = deviceInfoMapper;
    }

    /**
     * 记录当前在线连接数
     */
    private static final AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 记录socket连接
     */
    private static final LinkedHashMap<String, Session> socketMap = new LinkedHashMap<>();


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        // 在线数加1
        onlineCount.incrementAndGet();
        logger.info("有新连接加入，当前在线人数为：{}", onlineCount.get());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        // 在线数减1
        onlineCount.decrementAndGet();
        logger.info("用户退出，当前在线人数为：{}", onlineCount.get());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 客户端连接session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        logger.info("web socket receive: {}", message);
        if (StringUtils.isNotEmpty(message)) {
            JSONObject jsonObject = JSON.parseObject(message);
            String topic = jsonObject.getString("topic");
            if (DeviceSocketTopic.SOCKET_REGISTER.equals(topic)) {
                socketMap.put(jsonObject.getString("message"), session);
            } else if (DeviceSocketTopic.SOCKET_SYSTEM.equals(topic)) {
                // 记录本机设备信息
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setDeviceCode("service");
                deviceInfo.setDeviceJson(jsonObject.toJSONString());
                deviceInfo.setCreateTime(new Date());
                deviceInfoMapper.insert(deviceInfo);
            } else if (DeviceSocketTopic.SOCKET_HEART.equals(topic)) {

            }
        } else {
            onClose(session);
        }
    }

    /**
     * 服务端发送消息给客户端
     * @param clientName 客户端注册名称
     * @param socketMessage 发送消息内容
     * @param <T> 消息体数据类型
     */
    public <T> void sendMessage(String clientName, SocketMessage<T> socketMessage) {
        if (socketMap.containsKey(clientName)) {
            try {
                logger.info("web socket send: {}", JSONObject.toJSONString(socketMessage));
                Session session = socketMap.get(clientName);
                //如果开启@Async异步需要加锁，否则就会报错
                synchronized (session) {
                    session.getBasicRemote().sendText(JSON.toJSONString(socketMessage));
                }
            } catch (Exception e) {
                logger.error("web socket send error: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * web socket 连接异常
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        logger.error("web socket error: {}", error.getMessage(), error);
    }


}
