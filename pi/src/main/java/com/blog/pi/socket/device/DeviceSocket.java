package com.blog.pi.socket.device;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.mqtt.SpringUtils;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.enums.NettyPacketType;
import com.blog.pi.netty.enums.NettyTopicEnum;
import com.blog.pi.socket.SocketMessage;
import com.blog.pi.socket.device.domain.constant.DeviceSocketConstant;
import com.blog.pi.socket.device.domain.constant.DeviceSocketTopic;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: socket连接服务器py脚本数据监测socket
 * @Author: lxk
 * @date 2023/6/8 10:54
 */

@Slf4j
@Component
@ServerEndpoint("/python")
public class DeviceSocket {

    @Resource
    private NettyClient nettyClient = SpringUtils.getBean(NettyClient.class);

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
        log.info("有新连接加入，当前在线人数为：{}", onlineCount.get());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        // 在线数减1
        onlineCount.decrementAndGet();
        log.info("用户退出，当前在线人数为：{}", onlineCount.get());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        log.info("web socket msg :{}", message);
        if (StringUtils.isNotEmpty(message)) {
            JSONObject jsonObject = JSON.parseObject(message);
            String topic = jsonObject.getString("topic");
            if (DeviceSocketTopic.SOCKET_REGISTER.equals(topic)) {
                socketMap.put(jsonObject.getString("message"), session);
            } else if (DeviceSocketTopic.SOCKET_SYSTEM.equals(topic)) {
                // 发送 Netty 单片机设备注册消息
                NettyPacket<String> nettyRequest = NettyPacket.buildRequest(message);
                nettyRequest.setNettyPacketType(NettyPacketType.REQUEST.getValue());
                nettyRequest.setTopic(NettyTopicEnum.DEVICE_INFO.getTopic());
                nettyClient.sendMsg(JSONObject.toJSONString(nettyRequest));
            } else if (DeviceSocketTopic.SOCKET_HEART.equals(topic)) {

            }
        } else {
            onClose(session);
        }
    }

    @OnError
    public void onError(Throwable error) {
        log.error(error.getMessage());
    }

    /**
     * 服务端发送消息给客户端
     */
    public <T> void sendMessage(String clientName, SocketMessage<T> socketMessage) throws IOException {
        if (socketMap.containsKey(clientName)) {
            log.info("socket 发送消息");
            Session session = socketMap.get(clientName);
            //如果开启@Async异步需要加锁，否则就会报错
            synchronized (session) {
                session.getBasicRemote().sendText(JSON.toJSONString(socketMessage));
            }
        }
    }

    public <T> void sendMessage(Session session, SocketMessage<T> socketMessage) throws IOException {
        //如果开启@Async异步需要加锁，否则就会报错
        synchronized (session) {
            session.getBasicRemote().sendText(JSON.toJSONString(socketMessage));
        }

    }


}
