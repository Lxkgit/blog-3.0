package com.blog.pi.socket;

import com.alibaba.fastjson2.JSON;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.socket.SocketPacketEvent;
import com.blog.core.domain.socket.constant.SocketPacketType;
import com.blog.core.domain.socket.constant.SocketTopic;
import com.blog.core.domain.socket.dto.SocketMoveFileDto;
import com.blog.pi.netty.service.NettySyncFileService;
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

    @Resource
    private NettySyncFileService nettyFileSyncService;

    @Async
    @EventListener
    public void handleSocketReceiveMsgEvent(SocketPacketEvent event) {
        String type = event.getType();
        String id = event.getId();
        Session session = event.getSession();

        String requestId = event.getSocketPacket().getRequestId();
        String socketPacketType = event.getSocketPacket().getSocketPacketType();
        String topic = event.getSocketPacket().getTopic();
        MsgHead msgHead = event.getSocketPacket().getMsgHead();

        String data = event.getSocketPacket().getData().toString();
        if (!SocketPacketType.HEARTBEAT.equals(topic)) {
            // 心跳与系统检测上报消息不打印
            logger.info("===== socket 收到消息 ===== type: {} id: {} requestId: {} socketPacketType: {} topic: {} data: {}",
                    type, id, requestId, socketPacketType, topic, data);
        }
        if (SocketPacketType.REGISTER.equals(socketPacketType)) {

        } else if (SocketPacketType.HEARTBEAT.equals(socketPacketType)) {

        } else if (SocketPacketType.REQUEST.equals(socketPacketType)) {

        } else if (SocketPacketType.RESPONSE.equals(socketPacketType)) {
            if (SocketTopic.SOCKET_MOVE_FILE.equals(topic)) {
                SocketMoveFileDto dto = JSON.parseObject(data, SocketMoveFileDto.class);
                if (dto.getType().equals(2)) {
                    nettyFileSyncService.receiveSocketMoveFileMsg(dto, msgHead);
                }
            }
        }
    }
}
