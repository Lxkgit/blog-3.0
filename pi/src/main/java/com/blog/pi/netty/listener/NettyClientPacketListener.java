package com.blog.pi.netty.listener;

import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.netty.enums.NettyPacketType;
import com.blog.pi.netty.enums.NettyTopicEnum;
import com.blog.pi.netty.event.NettyPacketEvent;
import com.blog.pi.netty.service.SensorControlService;
import com.blog.pi.netty.service.SyncBlogFileService;
import io.netty.channel.ChannelId;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @Author: lxk
 * @date 2024/1/6 15:52
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyClientPacketListener implements ApplicationListener<NettyPacketEvent> {

    @Resource
    private SyncBlogFileService syncBlogFileService;

    @Resource
    private SensorControlService sensorControlService;

    @Async
    @Override
    public void onApplicationEvent(NettyPacketEvent event) {
        ChannelId channelId = (ChannelId) event.getSource();
        String nettyPacketType = event.getNettyPacket().getNettyPacketType();
        String requestId = event.getNettyPacket().getRequestId();
        String topic = event.getNettyPacket().getTopic();
        String registerId = event.getNettyPacket().getRegisterCode();
        String data = event.getNettyPacket().getData().toString();
        log.info("channelId:{} nettyPacketType:{} requestId:{} topic:{} registerId:{} data:{}", channelId, nettyPacketType, requestId, topic, registerId, data);
        if (nettyPacketType.equals(NettyPacketType.HEARTBEAT.getValue())) {
            // 服务器不会下发心跳信息，客户端心跳信息也不会响应
        } else if (nettyPacketType.equals(NettyPacketType.REQUEST.getValue())) {
            if (topic.equals(NettyTopicEnum.BLOG_FILE_SYNC.getTopic())) {
                // 处理文件下载同步
                syncBlogFileService.syncBlogFile(data, requestId);
            } else if (topic.equals(NettyTopicEnum.BLOG_SENSOR_CONTROL.getTopic())) {
                // 处理服务器控制命令
                sensorControlService.sendCommand(data, requestId);
            }
        } else if (nettyPacketType.equals(NettyPacketType.RESPONSE.getValue())) {
            // 处理netty消息发送后服务端响应数据

        } else {
            log.warn("unknown NettyPacketType!! channelId:{} event:{}", channelId, JSONObject.toJSONString(event));
        }
    }
}
