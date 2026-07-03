package com.blog.file.netty.service;


import com.alibaba.fastjson2.JSONObject;
import com.blog.file.mapper.DeviceMapper;
import com.blog.file.netty.domain.dto.NettyReplayMessage;
import com.blog.redis.constant.NettyRedisConstant;
import com.blog.redis.service.RedisService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;


@Component
@RequiredArgsConstructor
public class NettyServer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private Channel channel;
    // boss事件轮询线程组，处理连接事件
    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    // worker事件轮询线程组，用于数据处理
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private final NettyServerInitializer nettyServerInitializer;

    public static final AttributeKey<String> DEVICE_CODE = AttributeKey.valueOf("deviceCode");

    public static final Map<String, ChannelHandlerContext> CHANNEL_MAP = new ConcurrentHashMap<>();

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private RedisService redisService;

    @Value("${netty.port}")
    private Integer port;

    /**
     * 使用自定义线程池
     */
    @Resource
    private Executor baseThread;

    @Lazy
    @Resource
    private NettyMessageReplayThread replayThread;

    /**
     * 开启Netty服务
     */
    @Override
    public void run(String... args) {
        try {
            // 启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 设置参数，组配置
            serverBootstrap.group(bossGroup, workerGroup)
                    // 指定channel
                    .channel(NioServerSocketChannel.class)
                    // 初始化服务端可连接队列
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 允许重复使用本地地址和端口，连接关闭后，可以立即重用端口
                    .option(ChannelOption.SO_REUSEADDR, true)
                    // 设置TCP长连接，TCP会主动探测空闲连接的有效性
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 禁用Nagle算法，小数据时可以即时传输
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 发送缓冲区大小
                    .childOption(ChannelOption.SO_SNDBUF, 256 * 1024)
                    // 接收缓冲区大小
                    .childOption(ChannelOption.SO_RCVBUF, 256 * 1024)
                    // Netty服务端channel初始化
                    .childHandler(nettyServerInitializer);
            // 绑定端口，开始接收进来的连接
            ChannelFuture future = serverBootstrap.bind(port).sync();
            if (future.isSuccess()) {
                logger.info("===== Netty 服务端启动成功 ===== port: {}", port);
//                baseThread.execute(replayThread);
            }
            channel = future.channel();
        } catch (Exception e) {
            logger.error("===== Netty 服务端启动异常 ===== error: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    private void destroy() {
        if (channel != null) {
            channel.close();
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        logger.warn("Netty服务关闭");
    }

    public boolean channelWriteByDeviceCode(String deviceCode, String msg) {
        ChannelHandlerContext ctx = NettyServer.CHANNEL_MAP.get(deviceCode);
        if (ctx == null) {
            logger.warn("客户端: {} 不存在，消息发送异常", deviceCode);
            return false;
        }
        logger.info("===== netty 发送消息 ===== deviceCode:{}, msg:{}", deviceCode, msg);
        ctx.writeAndFlush(msg);
        return true;
    }

//    /**
//     * netty消息发送
//     *
//     * @param channelId 通道Id
//     * @param msg       发送消息
//     * @param retry     是否重发 是：true
//     * @return 消息是否发送成功
//     */
//    public boolean channelWriteByChannelId(ChannelId channelId, String registerId, String msg, boolean retry) {
//        ChannelHandlerContext ctx = NettyServerHandler.CHANNEL_MAP.get(channelId);
//        if (ctx == null) {
//            logger.warn("通道: {} 不存在，消息发送异常", channelId);
//            return false;
//        }
//        logger.info("===== netty 发送消息 ===== channelId:{}, registerId:{}, msg:{}, retry:{}", channelId, registerId, msg, retry);
//        ctx.writeAndFlush(msg);
//        return true;
//    }

//    /**
//     * netty 发送消息限制消息重发次数
//     *
//     * @param registerId
//     * @param requestId
//     * @param msg
//     * @param count
//     * @return
//     */
//    public boolean sendByRegisterIdLimitCount(String registerId, String requestId, String msg, Integer count) {
//        ChannelId channelId = NettyServerHandler.CLIENT_MAP.get(registerId);
//        NettyReplayMessage replayMessage = NettyReplayMessage.buildNettyReplayMessageLimitCount(count, msg);
//        redisService.setHash(NettyRedisConstant.NETTY_SEND_QUEUE, registerId, JSONObject.toJSONString(replayMessage), 4 * 60 * 60);
//        if (channelId == null) {
//            logger.warn("netty LimitCount 通道注册码:{} 不存在 msg:{}", registerId, msg);
//            return false;
//        }
//        return channelWriteByChannelId(channelId, registerId, msg, false);
//    }

    /**
     * netty 发送消息限制消息有效时间
     *
     * @param deviceCode 设备注册码
     * @param requestId  消息id
     * @param msg        netty发送消息
     * @param minute     消息有效时间
     * @return 消息发送结果
     */
    public boolean sendByRegisterIdLimitTime(String deviceCode, String requestId, String msg, Integer minute) {

        return channelWriteByDeviceCode(deviceCode, msg);
    }

    /**
     * netty 发送不需要重发的消息
     *
     * @param deviceCode
     * @param msg
     * @return
     */
    public boolean sendByRegisterIdNotRetry(String deviceCode, String msg) {

        return channelWriteByDeviceCode(deviceCode, msg);
    }


}
