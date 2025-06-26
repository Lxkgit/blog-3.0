package com.blog.file.netty.service;


import com.alibaba.fastjson2.JSONObject;
import com.blog.core.utils.MyStringUtils;
import com.blog.file.mapper.DeviceMapper;
import com.blog.file.netty.domain.dto.NettyReplayMessage;
import com.blog.redis.constant.NettyRedisConstant;
import com.blog.redis.service.RedisService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private RedisService redisService;

    @Value("${netty.port}")
    private Integer port;

    ExecutorService executor = Executors.newSingleThreadExecutor();

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
                logger.info("Netty 服务端启动成功 端口: {}", port);
                executor.execute(new NettyMessageReplayThread());
            }
            channel = future.channel();
        } catch (Exception e) {
            logger.error("Netty 服务端启动异常 error: {}", e.getMessage());
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

    /**
     * netty消息发送
     *
     * @param channelId 通道Id
     * @param msg       发送消息
     * @param retry     是否重发 是：true
     * @return 消息是否发送成功
     */
    public boolean channelWriteByChannelId(ChannelId channelId, String registerId, String msg, boolean retry) {
        ChannelHandlerContext ctx = NettyServerHandler.channelMap.get(channelId);
        if (ctx == null) {
            logger.warn("通道: {} 不存在，消息发送异常", channelId);
            return false;
        }
        logger.info("netty 发送消息, channelId:{}, registerId:{}, msg:{}, retry:{}", channelId, registerId, msg, retry);
        ctx.writeAndFlush(msg);
        return true;
    }

    /**
     * netty 发送消息限制消息重发次数
     *
     * @param registerId
     * @param msg
     * @param count
     * @return
     */
    public boolean sendByRegisterIdLimitCount(String registerId, String msg, Integer count) {
        ChannelId channelId = NettyServerHandler.clientMap.get(registerId);
        NettyReplayMessage replayMessage = new NettyReplayMessage();
        replayMessage.setRetryType(1);
        replayMessage.setLimitCount(count);
        replayMessage.setFirstSendTime(new Date());
        replayMessage.setMessage(msg);
        redisService.setHash(NettyRedisConstant.NETTY_SEND_QUEUE, registerId + "-" + MyStringUtils.getRandomString(6), JSONObject.toJSONString(replayMessage));
        if (channelId == null) {
            logger.warn("netty LimitCount 通道注册码:{} 不存在 msg:{}", registerId, msg);
            return false;
        }
        return channelWriteByChannelId(channelId, registerId, msg, false);
    }

    /**
     * netty 发送消息限制消息有效时间
     *
     * @param registerId
     * @param msg
     * @param minute
     * @return
     */
    public boolean sendByRegisterIdLimitTime(String registerId, String msg, Integer minute) {
        ChannelId channelId = NettyServerHandler.clientMap.get(registerId);
        NettyReplayMessage replayMessage = new NettyReplayMessage();
        replayMessage.setRetryType(2);
        replayMessage.setEffectiveTime(minute);
        replayMessage.setFirstSendTime(new Date());
        replayMessage.setMessage(msg);
        redisService.setHash(NettyRedisConstant.NETTY_SEND_QUEUE, registerId + "-" + MyStringUtils.getRandomString(6), JSONObject.toJSONString(replayMessage));
        if (channelId == null) {
            logger.warn("netty limitTime 通道注册码:{} 不存在 msg:{}", registerId, msg);
        }
        return channelWriteByChannelId(channelId, registerId, msg, false);
    }

    /**
     * netty 发送不需要重发的消息
     *
     * @param registerId
     * @param msg
     * @return
     */
    public boolean sendByRegisterIdNotRetry(String registerId, String msg) {
        ChannelId channelId = NettyServerHandler.clientMap.get(registerId);
        if (channelId == null) {
            logger.warn("netty notRetry 通道注册码:{} 不存在 msg:{}", registerId, msg);
            return false;
        }
        return channelWriteByChannelId(channelId, registerId, msg, false);
    }


}
