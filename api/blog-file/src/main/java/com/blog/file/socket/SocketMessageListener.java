package com.blog.file.socket;

import com.alibaba.fastjson2.JSON;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.core.domain.socket.SocketPacketEvent;
import com.blog.core.domain.socket.constant.SocketPacketType;
import com.blog.core.domain.socket.constant.SocketTopic;
import com.blog.core.domain.socket.dto.SocketDeleteFileOrDirDto;
import com.blog.file.socket.service.SystemInfoService;
import com.blog.file.task.BlogDateSyncTaskAction;
import jakarta.annotation.Resource;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * socket消息监听类
 *
 * @author 27992
 */
@Component
public class SocketMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(SocketMessageListener.class);


    @Resource
    private NettySyncFileService nettyFileSyncService;

    @Resource
    private SystemInfoService systemInfoService;

    @Resource
    private BlogDateSyncTaskAction blogDateSyncTaskAction;

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
            // 心跳与消息不打印
            logger.info("===== socket 收到消息 ===== type: {} id: {} requestId: {} socketPacketType: {} topic: {} data: {}",
                    type, id, requestId, socketPacketType, topic, data);
        }
        if (SocketPacketType.REGISTER.equals(socketPacketType)) {

        } else if (SocketPacketType.HEARTBEAT.equals(socketPacketType)) {

        } else if (SocketPacketType.REQUEST.equals(socketPacketType)) {
            if (SocketTopic.SYSTEM_INFO.equals(topic)) {
                systemInfoService.insertServiceInfoByPy(data);
            }
        } else if (SocketPacketType.RESPONSE.equals(socketPacketType)) {
            if (SocketTopic.SOCKET_EXPORT_BLOG_FILE.equals(topic)) {
                blogDateSyncTaskAction.syncBlogDataSecondStep(data, msgHead);
            } else if (SocketTopic.SOCKET_DELETE_FILE_OR_DIR.equals(topic)) {
                SocketDeleteFileOrDirDto dto = JSON.parseObject(data, SocketDeleteFileOrDirDto.class);
                nettyFileSyncService.receiveSocketDeleteFileMsg(dto, msgHead);
            }
        }

    }


}
