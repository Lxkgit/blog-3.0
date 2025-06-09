package com.blog.pi.socket;

import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.enums.NettyPacketType;
import com.blog.pi.netty.enums.NettyTopicEnum;
import com.blog.pi.netty.service.NettyFileSyncService;
import com.blog.pi.socket.device.domain.constant.DeviceSocketTopic;
import com.blog.pi.socket.device.domain.dto.MoveFileDto;
import jakarta.annotation.Resource;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SocketMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(SocketMessageListener.class);

    @Resource
    private NettyClient nettyClient;

    @Resource
    private NettyFileSyncService syncBlogFileService;

    @Async
    @EventListener
    public void handleSocketReceiveMsgEvent(SocketReceiveMessage receiveMsgEvent) {
        String type = receiveMsgEvent.getType();
        String id = receiveMsgEvent.getId();
        Session session = receiveMsgEvent.getSession();
        JSONObject jsonObject = JSONObject.parseObject(receiveMsgEvent.getMessage());
        String topic = jsonObject.getString("topic");
        logger.info("收到socket消息 type:{} id:{} topic:{} message: {}", type, id, topic, jsonObject);
        if (DeviceSocketTopic.SOCKET_REGISTER.equals(topic)) {
            String clientName = jsonObject.getString("message");

        } else if (DeviceSocketTopic.SOCKET_SYSTEM.equals(topic)) {
            NettyPacket<String> nettyRequest = NettyPacket.buildRequest(jsonObject.toString());
            nettyRequest.setNettyPacketType(NettyPacketType.REQUEST.getValue());
            nettyRequest.setTopic(NettyTopicEnum.DEVICE_INFO.getTopic());
            nettyClient.sendMsg(nettyRequest.getRequestId(), JSONObject.toJSONString(nettyRequest), true);
        } else if (DeviceSocketTopic.SOCKET_MOVE_FILE.equals(topic)) {
            MoveFileDto moveFileDto = new MoveFileDto();
            moveFileDto.setRequestId(jsonObject.getString("requestId"));
            moveFileDto.setSourceDirectory(jsonObject.getString("sourceDirectory"));
            moveFileDto.setTargetDirectory(jsonObject.getString("targetDirectory"));
            List<String> fileNameList = jsonObject.getJSONObject("message").getList("file_list", String.class);
            syncBlogFileService.updateBlogFileSecondStep(moveFileDto, fileNameList);
        }
    }



}
