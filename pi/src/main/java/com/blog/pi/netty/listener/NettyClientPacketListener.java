package com.blog.pi.netty.listener;

import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.domain.common.MsgHead;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.enums.NettyPacketType;
import com.blog.pi.netty.enums.NettyTopic;
import com.blog.pi.netty.enums.NettyTopicEnum;
import com.blog.pi.netty.event.NettyPacketEvent;
import com.blog.pi.netty.service.SensorControlService;
import com.blog.pi.netty.service.NettyFileSyncService;
import com.blog.redis.constant.NettyRedisConstant;
import com.blog.redis.service.RedisService;
import io.netty.channel.ChannelId;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @Author: lxk
 * @date 2024/1/6 15:52
 */

@Component
@RequiredArgsConstructor
public class NettyClientPacketListener implements ApplicationListener<NettyPacketEvent> {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientPacketListener.class);

    @Resource
    private NettyFileSyncService syncBlogFileService;

    @Resource
    private SensorControlService sensorControlService;

    @Resource
    private RedisService redisService;

    @Resource
    private NettyClient nettyClient;


    @Async
    @Override
    public void onApplicationEvent(NettyPacketEvent event) {
        ChannelId channelId = (ChannelId) event.getSource();
        String nettyPacketType = event.getNettyPacket().getNettyPacketType();
        String requestId = event.getNettyPacket().getRequestId();
        String topic = event.getNettyPacket().getTopic();
        String registerId = event.getNettyPacket().getRegisterCode();
        String data = event.getNettyPacket().getData().toString();
        MsgHead msgHead = event.getNettyPacket().getMsgHead();
        logger.info("===== netty 收到消息 ===== msgHead: {} channelId:{} nettyPacketType:{} requestId:{} topic:{} registerId:{} data:{}",
                msgHead, channelId, nettyPacketType, requestId, topic, registerId, data);
        if (nettyPacketType.equals(NettyPacketType.HEARTBEAT.getValue())) {
            // 服务器不会下发心跳信息，客户端心跳信息也不会响应
        } else if (nettyPacketType.equals(NettyPacketType.REQUEST.getValue())) {
            if (NettyTopic.BLOG_FILE_SYNC.equals(topic)) {
                // 处理文件下载同步
                syncBlogFileService.syncBlogFile(data, requestId, msgHead);
            } else if (NettyTopic.BLOG_SENSOR_CONTROL.equals(topic)) {
                // 处理服务器控制命令
                sensorControlService.sendCommand(data, requestId, msgHead);
            } else if (topic.equals(NettyTopicEnum.BLOG_FILE_UPLOAD.getTopic())) {
                // 处理文件上传消息
//                syncBlogFileService.uploadBlogFileFirstStep(data, requestId);
            }

//            if (topic.equals(NettyTopicEnum.BLOG_FILE_SYNC.getTopic())) {
//                // 处理文件下载同步
//                syncBlogFileService.syncBlogFile(data, requestId);
//            } else if (topic.equals(NettyTopicEnum.BLOG_FILE_UPLOAD.getTopic())) {
//                syncBlogFileService.uploadBlogFileFirstStep(data, requestId);
//            } else if (topic.equals(NettyTopicEnum.BLOG_SENSOR_CONTROL.getTopic())) {
//                // 处理服务器控制命令
//                sensorControlService.sendCommand(data, requestId);
//            }
        } else if (nettyPacketType.equals(NettyPacketType.RESPONSE.getValue())) {
            // 处理netty消息发送后服务端响应数据
            redisService.setSet(NettyRedisConstant.NETTY_RECEIVE_QUEUE, requestId);
        } else {
            logger.warn("unknown NettyPacketType channelId:{} event:{}", channelId, JSONObject.toJSONString(event));
        }
    }
}
