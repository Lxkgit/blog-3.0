package com.blog.file.socket;

import com.alibaba.fastjson2.JSON;
import com.blog.core.domain.common.MsgHead;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.file.socket.domain.SocketPacketEvent;
import com.blog.file.socket.domain.constant.SocketPacketType;
import com.blog.file.socket.domain.constant.SocketTopic;
import com.blog.file.socket.domain.dto.SocketDeleteFileOrDirDto;
import jakarta.annotation.Resource;
import jakarta.websocket.Session;
import org.apache.commons.lang3.StringUtils;
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
        if (!SocketPacketType.HEARTBEAT.equals(topic) && !"system".equals(topic)) {
            // 心跳与系统上报消息不打印
            logger.info("===== socket 收到消息 ===== type: {} id: {} requestId: {} socketPacketType: {} topic: {} data: {}",
                    type, id, requestId, socketPacketType, topic, data);
        }
        if (SocketPacketType.REGISTER.equals(socketPacketType)) {

        } else if (SocketPacketType.HEARTBEAT.equals(socketPacketType)) {

        } else if (SocketPacketType.REQUEST.equals(socketPacketType)) {

        } else if (SocketPacketType.RESPONSE.equals(socketPacketType)) {
            if (SocketTopic.SOCKET_EXPORT_BLOG_FILE.equals(topic)) {
                nettyFileSyncService.syncBlogDataSecondStep(data, msgHead);
            } else if (SocketTopic.SOCKET_DELETE_FILE_OR_DIR.equals(topic)) {
                SocketDeleteFileOrDirDto dto = JSON.parseObject(data, SocketDeleteFileOrDirDto.class);
                nettyFileSyncService.receiveSocketDeleteFileMsg(dto, msgHead);
            }
        }

    }


}
