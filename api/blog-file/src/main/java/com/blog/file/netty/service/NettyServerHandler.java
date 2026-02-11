package com.blog.file.netty.service;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.blog.core.domain.file.device.entity.Device;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.mapper.DeviceMapper;
import com.blog.file.netty.event.NettyPacketEvent;
import com.blog.redis.constant.NettyRedisConstant;
import com.blog.redis.service.RedisService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @description: Netty服务端处理器
 * @Author: lxk
 * @date 2024/1/6 15:16
 */

@Component
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private RedisService redisService;

    @Resource
    private DeviceMapper deviceMapper;

    /**
     * 当客户端主动连接服务端，通道活跃后触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = inetSocketAddress.getAddress().getHostAddress();
        int clientPort = inetSocketAddress.getPort();
        // 获取连接通道唯一标识
        ChannelId channelId = ctx.channel().id();

    }

    /**
     * 当客户端主动断开连接，通道不活跃触发
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = inetSocketAddress.getAddress().getHostAddress();
        int clientPort = inetSocketAddress.getPort();
        // 获取终止连接的客户端ID
        ChannelId channelId = ctx.channel().id();
        logger.info("客户端: {} 断开连接", ctx.channel().attr(NettyServer.DEVICE_CODE).get());
        NettyServer.CHANNEL_MAP.remove(ctx.channel().attr(NettyServer.DEVICE_CODE).get());
    }

    /**
     * 通道有消息触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {

            // 报文解析处理
            // 处理泛型：new TypeReference<NettyPacket<Object>>() {}.getType()
            // TypeReference：解决Java泛型类型擦除问题，保留NettyPacket<Object>的类型信息，确保反序列化时能正确识别泛型类型
            // NettyPacket：自定义的泛型类，可能用于封装网络传输的数据包，Object表示其携带的数据类型可以是任意对象

            NettyPacket<Object> nettyPacket = JSONObject.parseObject(msg.toString(), new TypeReference<NettyPacket<Object>>() {
            }.getType());
            // 发布自定义Netty数据包处理事件
            applicationEventPublisher.publishEvent(new NettyPacketEvent(ctx, nettyPacket));
        } catch (Exception e) {
            logger.error("netty msg:{} 数据解析异常:{}", msg.toString(), e.getMessage(), e);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            // 只处理 READER_IDLE，避免 WRITER_IDLE/ALL_IDLE 错误关闭
            if (event.state() == IdleState.READER_IDLE) {
            }
        } else {
            // 对于非 IdleStateEvent 的其他事件，继续向下传递给 pipeline 中的下一个 handler
            ctx.fireUserEventTriggered(evt);
        }
    }


    /**
     * 当连接发生异常时触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("exceptionCaught 异常断开 netty 通道连接");
        // 当出现异常就关闭连接
        ctx.close();
    }

    /**
     * 校验设备编码是否存在于设备表中
     *
     * @param deviceCode
     * @return
     */
    public boolean checkContainByDeviceCode(String deviceCode) {
        Set<String> deviceCodeSet;
        if (redisService.hasKey(NettyRedisConstant.NETTY_DEVICE_CODE)) {
            Set<Object> set = redisService.getSetByKey(NettyRedisConstant.NETTY_DEVICE_CODE);
            deviceCodeSet = set.stream().filter(Objects::nonNull).map(String::valueOf).collect(Collectors.toCollection(HashSet::new));
        } else {
            List<Device> deviceList = deviceMapper.selectList(null);
            deviceCodeSet = deviceList.stream().map(Device::getDeviceCode).collect(Collectors.toSet());
            redisService.setSet(NettyRedisConstant.NETTY_DEVICE_CODE, deviceCodeSet.toArray());
        }
        return deviceCodeSet.contains(deviceCode);
    }

}
