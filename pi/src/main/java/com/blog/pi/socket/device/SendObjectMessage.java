package com.blog.pi.socket.device;

import com.alibaba.fastjson2.JSON;
import com.blog.pi.socket.SocketMessage;
import com.blog.pi.socket.device.domain.enums.SocketTopicEnum;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: socket连接服务器py脚本数据监测socket
 * @Author: lxk
 * @date 2023/6/8 10:54
 */

@Slf4j
@Component
@ServerEndpoint("/python")
public class SendObjectMessage {

    /** 记录当前在线连接数 */
    private static final AtomicInteger onlineCount = new AtomicInteger(0);
    private static final CopyOnWriteArraySet<SendObjectMessage> socket = new CopyOnWriteArraySet<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        socket.add(this);
        onlineCount.incrementAndGet(); // 在线数加1
        try {
            SocketMessage<String> socketMessage = new SocketMessage<>();
            socketMessage.setTopic(SocketTopicEnum.SOCKET_SYSTEM.getTopic());
            socketMessage.setMessage("连接成功");
            sendMessage(JSON.toJSONString(socketMessage));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.info("有新连接加入，当前在线人数为：{}", onlineCount.get());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        socket.remove(this);
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
        if (message != null && !message.isEmpty()) {
            for (SendObjectMessage sendObjectMessage : socket) {
                if (session.equals(socket)) {
                    sendObjectMessage.sendMessage(message);
                }
                String topic = (String) JSON.parseObject(message).get("topic");
                if(topic.equals("heart")) {
                    sendObjectMessage.sendMessage("{\"msg\":\"hearBeat\",\"topic\":\"heart\"}");
                }
            }
        }
        log.info("服务端收到客户端的消息:{}", message);
    }

    @OnError
    public void onError(Throwable error) {
        log.error(error.getMessage());
    }

    /**
     * 服务端发送消息给客户端
     */
    public void sendMessage(String message) throws IOException {
        //如果开启@Async异步需要加锁，否则就会报错
        synchronized (session){
            this.session.getBasicRemote().sendText(message);
        }
    }



}
