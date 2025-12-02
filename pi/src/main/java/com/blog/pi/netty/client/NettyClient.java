package com.blog.pi.netty.client;


import com.alibaba.fastjson2.JSONObject;
import com.blog.core.utils.MyStringUtils;
import com.blog.pi.config.PiSystemConfig;
import com.blog.pi.mapper.RegisterSettingMapper;
import com.blog.pi.netty.dto.NettyReplayMessage;
import com.blog.redis.constant.NettyRedisConstant;
import com.blog.redis.service.RedisService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @Author: lxk
 * @date 2024/1/6 15:51
 */

@Component
@RequiredArgsConstructor
public class NettyClient implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private Channel channel;
    private final EventLoopGroup workGroup = new NioEventLoopGroup();
    private final NettyClientInitializer nettyClientInitializer;

    @Resource
    private RegisterSettingMapper registerSettingDAO;

    @Resource
    private PiSystemConfig piSystemConfig;

    @Resource
    private RedisService redisService;

    @Value("${netty.ip}")
    private String ip;

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

    @Override
    public void run(String... args) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    // 设置TCP长连接，TCP会主动探测空闲连接的有效性
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // 禁用Nagle算法，小数据时可以即时传输
                    .option(ChannelOption.TCP_NODELAY, true)
                    // 发送缓冲区大小
                    .option(ChannelOption.SO_SNDBUF, 256 * 1024)
                    // 接收缓冲区大小
                    .option(ChannelOption.SO_RCVBUF, 256 * 1024)
                    // Netty客户端channel初始化
                    .handler(nettyClientInitializer);
            // 连接服务器ip、端口
            ChannelFuture future = bootstrap.connect(ip, port);

            //客户端断线重连逻辑
            future.addListener((ChannelFutureListener) futureListener -> {
                if (futureListener.isSuccess()) {
                    logger.info("===== netty 连接成功 =====");
//                    baseThread.execute(replayThread);
                } else {
                    logger.warn("===== netty 连接失败，30秒后尝试重新连接 =====");
                    futureListener.channel().eventLoop().schedule((Runnable) this::run, 30, TimeUnit.SECONDS);
                }
            });
            channel = future.channel();
        } catch (Exception e) {
            logger.error("连接Netty服务端异常 error:{}", e.getMessage(), e);
        }
    }

    @PreDestroy
    private void destroy() {
        if (channel != null) {
            channel.close();
        }
        workGroup.shutdownGracefully();
        logger.warn("netty 服务关闭");
    }

    /**
     * netty向服务端发送消息
     * @param requestId 消息id
     * @param msg 消息
     * @param retry 是否重发
     */
    public void sendMsg(String requestId, String msg, boolean retry) {
        logger.info("===== netty 发送消息 ===== requestId: {} msg: {} retry: {}", requestId, msg, retry);
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(msg);
        } else {
            logger.warn("===== netty 连接已断开 ===== requestId: {} msg: {} retry: {}", requestId, msg, retry);
        }
        if (retry) {
            NettyReplayMessage replayMessage = NettyReplayMessage.buildNettyReplayMessageLimitCount(10, msg);
            redisService.setHash(NettyRedisConstant.NETTY_SEND_QUEUE, requestId, JSONObject.toJSONString(replayMessage));
        }
    }

    public boolean getChannelActive() {
        return channel.isActive();
    }
}

