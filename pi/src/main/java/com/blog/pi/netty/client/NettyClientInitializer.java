package com.blog.pi.netty.client;


import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Netty客户端初始化
 * @author 27992
 */
@Component
@RequiredArgsConstructor
public class NettyClientInitializer extends ChannelInitializer<Channel> {


    private final NettyClientHandler nettyClientHandler;

    @Override
    protected void initChannel(Channel channel) {

        channel.pipeline()

                /*
                 * 解码器
                 * 2字节长度字段
                 */
                .addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2))

                /*
                 * 编码器
                 * 自动添加消息长度
                 */
                .addLast(new LengthFieldPrepender(2))

                /*
                 * 字符串编解码
                 */
                .addLast(new StringDecoder(CharsetUtil.UTF_8))
                .addLast(new StringEncoder(CharsetUtil.UTF_8))

                /*
                 * 心跳检测
                 * readerIdleTime: 0 不检测
                 * writerIdleTime:  30秒没有发送数据触发WRITER_IDLE
                 * allIdleTime: 0
                 */
                .addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS))

                /*
                 * 业务handler
                 */
                .addLast(nettyClientHandler);


    }


}

