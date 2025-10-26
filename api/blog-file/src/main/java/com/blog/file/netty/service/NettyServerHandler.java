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
    public static final Map<ChannelId, ChannelHandlerContext> channelMap = new ConcurrentHashMap<>();

    // 全局map，保存客户端编码与netty通道编码 （用于服务端指定客户端发送消息）
    public static final Map<String, ChannelId> clientMap = new ConcurrentHashMap<>();

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
        if (channelMap.containsKey(channelId)) {
            logger.info("客户端【{}】是连接状态，连接通道数量:{}", channelId, channelMap.size());
        } else {
            // 保存连接
            channelMap.put(channelId, ctx);
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
        if (channelMap.containsKey(channelId)) {
            // 删除连接
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
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        String socketString = ctx.channel().remoteAddress().toString();
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                logger.warn("Client: {} READER_IDLE 读超时", socketString);
                ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                logger.warn("Client: {} WRITER_IDLE 写超时", socketString);
                ctx.close();
            } else if (event.state() == IdleState.ALL_IDLE) {
                logger.warn("Client: {} ALL_IDLE 读/写超时", socketString);
                ctx.close();
            }
        }
    }

    /**
     * 当连接发生异常时触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        for (ChannelId channelId : channelMap.keySet()) {
            if (channelMap.get(channelId).equals(ctx)) {
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
        if (NettyServerHandler.channelMap.containsKey(channelId)) {
            // 断开netty连接
            ChannelHandlerContext ctx = NettyServerHandler.channelMap.get(channelId);
            ctx.close();

            for (String deviceCode : NettyServerHandler.clientMap.keySet()) {
                if (NettyServerHandler.clientMap.get(deviceCode).equals(channelId)) {
                    NettyServerHandler.clientMap.remove(deviceCode);
                }
            }
            NettyServerHandler.channelMap.remove(channelId);
        }
    }

    /**
     * 根据设备编码移除通道
     *
     * @param deviceCode
     */
    public void closeChannelByDeviceCode(String deviceCode) {
        if (NettyServerHandler.clientMap.containsKey(deviceCode)) {
            ChannelId channelId = NettyServerHandler.clientMap.get(deviceCode);

            // 断开netty连接
            ChannelHandlerContext ctx = NettyServerHandler.channelMap.get(channelId);
            ctx.close();

            NettyServerHandler.channelMap.remove(channelId);
            NettyServerHandler.clientMap.remove(deviceCode);
        }
    }
}
