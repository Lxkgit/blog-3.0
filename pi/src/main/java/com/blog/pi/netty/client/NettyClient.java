package com.blog.pi.netty.client;


import com.blog.pi.config.PiSystemConfig;
import com.blog.pi.dao.RegisterSettingDAO;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @Author: lxk
 * @date 2024/1/6 15:51
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyClient implements CommandLineRunner {

    private Channel channel;
    private final EventLoopGroup workGroup = new NioEventLoopGroup();
    private final NettyClientInitializer nettyClientInitializer;

    @Resource
    private RegisterSettingDAO registerSettingDAO;

    @Resource
    private PiSystemConfig piSystemConfig;

    @Resource
    private RedisService redisService;



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
            ChannelFuture future = bootstrap.connect((String) piSystemConfig.getRegisterConfig("netty", "ip"),
                    (Integer) piSystemConfig.getRegisterConfig("netty", "port"));

            //客户端断线重连逻辑
            future.addListener((ChannelFutureListener) futureListener -> {
                if (futureListener.isSuccess()) {
                    log.info("netty connection success");
                } else {
                    log.warn("netty connection failed, try again after 30 seconds");
                    futureListener.channel().eventLoop().schedule((Runnable) this::run, 30, TimeUnit.SECONDS);
                }
            });
            channel = future.channel();
        } catch (Exception e) {
            log.error("连接Netty服务端异常!! error:{}", e.getMessage());
        }
    }

    @PreDestroy
    private void destroy() {
        if (channel != null) {
            channel.close();
        }
        workGroup.shutdownGracefully();
        log.warn("netty service close");
    }

    /**
     * netty向服务端发送消息
     * @param requestId 消息id
     * @param msg 消息
     * @param retry 是否重发
     */
    public void sendMsg(String requestId, String msg, boolean retry) {
        boolean active = channel.isActive();
        if (active) {
            channel.writeAndFlush(msg);
        } else {
            log.warn("netty 连接已断开");
        }
        if (retry) {
            NettyReplayMessage nettyReplayMessage = new NettyReplayMessage(msg);
            redisService.setHash(NettyRedisConstant.NETTY_SEND_QUEUE, requestId, nettyReplayMessage);
        }
    }

    public boolean getChannelActive() {
        return channel.isActive();
    }
}

