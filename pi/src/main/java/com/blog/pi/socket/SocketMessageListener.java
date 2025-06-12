package com.blog.pi.socket;

import com.blog.pi.socket.domain.SocketPacketEvent;
import com.blog.pi.socket.domain.constant.SocketPacketType;
import jakarta.annotation.Resource;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SocketMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(SocketMessageListener.class);

    @Async
    @EventListener
    public void handleSocketReceiveMsgEvent(SocketPacketEvent event) {
        String type = event.getType();
        String id = event.getId();
        Session session = event.getSession();

        String requestId = event.getSocketPacket().getRequestId();
        String socketPacketType = event.getSocketPacket().getSocketPacketType();
        String topic = event.getSocketPacket().getTopic();

        String data = event.getSocketPacket().getData().toString();
        logger.info("socket 收到消息，type:{} id:{} requestId:{} socketPacketType:{} topic:{} data:{}",
                type, id, requestId, socketPacketType, topic, data);
        if (SocketPacketType.REGISTER.equals(socketPacketType)) {

        } else if (SocketPacketType.HEARTBEAT.equals(socketPacketType)) {

        } else if (SocketPacketType.REQUEST.equals(socketPacketType)) {

        } else if (SocketPacketType.RESPONSE.equals(socketPacketType)) {

        }

    }



}
