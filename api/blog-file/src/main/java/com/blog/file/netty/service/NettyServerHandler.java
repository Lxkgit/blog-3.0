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

    // 全局map，保存通道编码与客户端编码 （用户服务端监听到客户端断开连接，移除map通道）
    public static final Map<ChannelId, ChannelHandlerContext> CHANNEL_MAP = new ConcurrentHashMap<>();

    // 全局map，保存客户端编码与netty通道编码 （用于服务端指定客户端发送消息）
    public static final Map<String, ChannelId> CLIENT_MAP = new ConcurrentHashMap<>();

    private final Map<ChannelId, Integer> idleCountMap = new ConcurrentHashMap<>();

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
        // 如果map中不包含此连接，就保存连接
        if (CHANNEL_MAP.containsKey(channelId)) {
            logger.info("客户端【{}】是连接状态，连接通道数量:{}", channelId, CHANNEL_MAP.size());
        } else {
            // 保存连接
            CHANNEL_MAP.put(channelId, ctx);
            logger.info("netty client【{}】 connected [clientIp:{} clientPort:{}]", channelId, clientIp, clientPort);
        }
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
        // 包含此客户端才去删除
        if (CHANNEL_MAP.containsKey(channelId)) {
            // 删除连接
            logger.error("channelInactive 异常断开 netty 通道连接");
            closeChannelByChannelId(channelId);
            logger.warn("客户端【{}】断开Netty连接!![clientIp:{} clientPort:{}]", channelId, clientIp, clientPort);
        }
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
            applicationEventPublisher.publishEvent(new NettyPacketEvent(ctx.channel().id(), nettyPacket));
        } catch (Exception e) {
            logger.error("netty msg:{} 数据解析异常:{}", msg.toString(), e.getMessage(), e);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            // 只处理 READER_IDLE，避免 WRITER_IDLE/ALL_IDLE 错误关闭
            if (event.state() == IdleState.READER_IDLE) {
                int count = idleCountMap.getOrDefault(ctx.channel().id(), 0) + 1;
                idleCountMap.put(ctx.channel().id(), count);

                // 打印每次超时日志
                logger.warn("Client {} READER_IDLE 第 {} 次", ctx.channel().remoteAddress(), count);

                // 连续超过 5 次才关闭连接
                if (count >= 5) {
                    logger.warn("Client {} 连续5次 READER_IDLE, 关闭连接", ctx.channel().remoteAddress());
                    ctx.close();
                    idleCountMap.remove(ctx.channel().id());
                }
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
        for (ChannelId channelId : CHANNEL_MAP.keySet()) {
            if (CHANNEL_MAP.get(channelId).equals(ctx)) {
                closeChannelByChannelId(channelId);
            }
        }
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

    /**
     * 根据通道id移除netty客户端连接
     *
     * @param channelId
     */
    public void closeChannelByChannelId(ChannelId channelId) {
        if (NettyServerHandler.CHANNEL_MAP.containsKey(channelId)) {
            // 断开netty连接
            ChannelHandlerContext ctx = NettyServerHandler.CHANNEL_MAP.get(channelId);
            ctx.close();

            for (String deviceCode : NettyServerHandler.CLIENT_MAP.keySet()) {
                if (NettyServerHandler.CLIENT_MAP.get(deviceCode).equals(channelId)) {
                    logger.error("closeChannelByChannelId 断开 netty 通道连接 deviceCode:{}", deviceCode);
                    NettyServerHandler.CLIENT_MAP.remove(deviceCode);
                }
            }
            NettyServerHandler.CHANNEL_MAP.remove(channelId);
        }
    }

    /**
     * 根据设备编码移除通道
     *
     * @param deviceCode
     */
    public void closeChannelByDeviceCode(String deviceCode) {
        if (NettyServerHandler.CLIENT_MAP.containsKey(deviceCode)) {
            ChannelId channelId = NettyServerHandler.CLIENT_MAP.get(deviceCode);

            // 断开netty连接
            ChannelHandlerContext ctx = NettyServerHandler.CHANNEL_MAP.get(channelId);
            ctx.close();
            logger.error("closeChannelByDeviceCode 断开 netty 通道连接 deviceCode:{}", deviceCode);
            NettyServerHandler.CHANNEL_MAP.remove(channelId);
            NettyServerHandler.CLIENT_MAP.remove(deviceCode);
        }
    }
}
