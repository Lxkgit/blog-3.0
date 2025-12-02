package com.blog.pi.netty.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.domain.common.MsgHead;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.dto.file.NettySyncFileDto;
import com.blog.pi.netty.enums.NettyPacketType;
import com.blog.pi.netty.enums.NettyTopic;
import com.blog.pi.netty.enums.NettyTopicEnum;
import com.blog.pi.netty.event.NettyPacketEvent;
import com.blog.pi.netty.service.SensorControlService;
import com.blog.pi.netty.service.NettySyncFileService;
import com.blog.redis.constant.NettyRedisConstant;
import com.blog.redis.service.RedisService;
import io.netty.channel.ChannelId;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
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
    private NettySyncFileService syncBlogFileService;

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
        MsgHead msgHead = event.getNettyPacket().getMsgHead();

        String nettyPacketType = msgHead.getNettyMsgHead().getNettyPacketType();
        String requestId = msgHead.getNettyMsgHead().getRequestId();
        String topic = msgHead.getNettyMsgHead().getTopic();
        String registerCode = msgHead.getNettyMsgHead().getRegisterCode();

        String data = event.getNettyPacket().getData().toString();

        logger.info("===== netty 收到消息 ===== msgHead: {} channelId:{} nettyPacketType:{} requestId:{} topic:{} registerCode:{} data:{}",
                msgHead, channelId, nettyPacketType, requestId, topic, registerCode, data);
        if (nettyPacketType.equals(NettyPacketType.HEARTBEAT.getValue())) {
            // 服务器不会下发心跳信息，客户端心跳信息也不会响应
        } else if (nettyPacketType.equals(NettyPacketType.REQUEST.getValue())) {
            // 回复请求消息响应(业务内部可以会再次响应消息，此响应防止服务器重发消息)
            NettyPacket<String> nettyResponse = NettyPacket.buildResponse(requestId, topic, "response");
            nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyResponse), false);

            if (NettyTopic.BLOG_FILE_SYNC.equals(topic)) {
                // 处理文件同步消息
                NettySyncFileDto nettySyncBlogFile = JSON.parseObject(data, NettySyncFileDto.class);
                syncBlogFileService.receiveSyncFileMsg(msgHead, nettySyncBlogFile);
            } else if (NettyTopic.BLOG_SENSOR_CONTROL.equals(topic)) {
                // 处理服务器控制命令
                sensorControlService.sendCommand(data, requestId, msgHead);
            } else if (topic.equals(NettyTopicEnum.BLOG_FILE_UPLOAD.getTopic())) {
                // 处理文件上传消息
//                syncBlogFileService.uploadBlogFileFirstStep(data, requestId);
            }
        } else if (nettyPacketType.equals(NettyPacketType.RESPONSE.getValue())) {
            // 记录响应类消息记录消息序列号，取消对此消息重发
            redisService.setSet(NettyRedisConstant.NETTY_RECEIVE_QUEUE, requestId);
        } else {
            logger.warn("unknown NettyPacketType channelId:{} event:{}", channelId, JSONObject.toJSONString(event));
        }
    }
}
