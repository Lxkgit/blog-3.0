package com.blog.pi.netty.client;


import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.netty.dto.NettyReplayMessage;
import com.blog.core.utils.MyStringUtils;
import com.blog.pi.config.PiSystemConfig;
import com.blog.pi.mapper.RegisterSettingMapper;
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

    private volatile Channel channel;

    private final EventLoopGroup workGroup = new NioEventLoopGroup();

    private final NettyClientInitializer nettyClientInitializer;

    @Value("${netty.ip}")
    private String ip;

    @Value("${netty.port}")
    private Integer port;

    /**
     * 是否正在连接
     */
    private volatile boolean connecting = false;

    @Override
    public void run(String... args) {
        connect();
    }


    /**
     * 真正连接方法
     */
    private synchronized void connect() {
        // 已经存在连接
        if (channel != null && channel.isActive()) {
            logger.warn("===== Netty已有连接，不重复连接 channel:{} =====", channel.id().asShortText());
            return;
        }
        // 正在连接
        if (connecting) {
            logger.warn("===== Netty正在连接，跳过 =====");
            return;
        }
        connecting = true;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, 256 * 1024)
                .option(ChannelOption.SO_RCVBUF, 256 * 1024)
                .handler(nettyClientInitializer);
        logger.info("===== Netty开始连接 {}:{} =====", ip, port);
        bootstrap.connect(ip, port).addListener((ChannelFutureListener) future -> {
            connecting = false;
            if (future.isSuccess()) {
                channel = future.channel();
                logger.info("===== Netty连接成功 channel:{} =====", channel.id().asShortText());
            } else {
                logger.error("===== Netty连接失败，30秒后重试 =====");
                scheduleReconnect();
            }
        });
    }


    /**
     * 统一重连
     */
    public void scheduleReconnect() {
        workGroup.schedule(this::connect, 30, TimeUnit.SECONDS);

    }


    /**
     * Handler调用
     */
    public void channelInactive(Channel inactiveChannel) {
        if (channel == inactiveChannel) {
            logger.warn("===== 当前Netty连接断开 channel:{} =====", inactiveChannel.id().asShortText());
            channel = null;
            scheduleReconnect();
        } else {
            logger.warn("===== 旧Netty连接断开，不处理 channel:{} =====", inactiveChannel.id().asShortText());
        }
    }


    /**
     * 原方法保持
     */
    public void sendMsg(String requestId, String msg, boolean retry) {
        logger.info("===== netty发送消息 requestId:{} retry:{} =====", requestId, retry);
        Channel currentChannel = channel;
        if (currentChannel != null && currentChannel.isActive()) {
            currentChannel.writeAndFlush(msg);
        } else {
            logger.warn("===== netty连接已断开 requestId:{} =====", requestId);
        }
    }

    @PreDestroy
    private void destroy() {
        if (channel != null) {
            channel.close();
        }
        workGroup.shutdownGracefully();
        logger.warn("===== netty服务关闭 =====");
    }
}