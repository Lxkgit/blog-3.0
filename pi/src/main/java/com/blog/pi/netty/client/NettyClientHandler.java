package com.blog.pi.netty.client;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.blog.core.domain.netty.dto.NettyPacket;
import com.blog.core.domain.netty.dto.heart.NettyHeartBeatDto;
import com.blog.core.domain.netty.dto.register.NettyRegisterDto;
import com.blog.core.domain.netty.enums.HeartBeatType;
import com.blog.core.domain.netty.enums.NettyPacketType;
import com.blog.pi.netty.event.NettyPacketEvent;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;


/**
 * @description:
 * @Author: lxk
 * @date 2024/1/6 15:52
 */

@Component
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Lazy
    @Resource
    private NettyClient nettyClient;

    /**
     * TCP连接成功
     * 每个channel只执行一次
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("===== Netty连接建立 channel:{} =====", ctx.channel().id().asShortText());
        sendRegister(ctx);
    }


    /**
     * 发送注册
     */
    private void sendRegister(ChannelHandlerContext ctx) {
        NettyRegisterDto dto = new NettyRegisterDto();
        dto.setDeviceName("树莓派");
        dto.setMemo("这个是设备备注信息");
        NettyPacket<NettyRegisterDto> packet = NettyPacket.buildRequest(NettyPacketType.REGISTER, NettyPacketType.REGISTER.getValue(),
                dto, "1:2ecfb95116de4967afe7710e11ac00b4");

        String msg = JSONObject.toJSONString(packet);
        ctx.writeAndFlush(msg);
        logger.info("===== Netty发送注册 channel:{} =====", ctx.channel().id().asShortText());
    }

    /**
     * TCP断开
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.warn("===== Netty连接断开 channel:{} =====", ctx.channel().id().asShortText());
        nettyClient.channelInactive(ctx.channel());
    }


    /**
     * 心跳
     * <p>
     * IdleStateHandler(0,30,0)
     * 30秒没有写操作触发
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleEvent) {
            if (idleEvent.state() == IdleState.WRITER_IDLE) {
                sendHeartbeat(ctx);
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }


    /**
     * 发送心跳
     */
    private void sendHeartbeat(ChannelHandlerContext ctx) {
        NettyHeartBeatDto dto = new NettyHeartBeatDto();
        dto.setHeartBeat(new Date());
        dto.setType(HeartBeatType.SERVICE.getType());
        NettyPacket<NettyHeartBeatDto> packet = NettyPacket.buildRequest(NettyPacketType.HEARTBEAT, NettyPacketType.HEARTBEAT.getValue(),
                        dto, "1:2ecfb95116de4967afe7710e11ac00b4");
        String msg = JSONObject.toJSONString(packet);
        ctx.writeAndFlush(msg);
        logger.debug("===== Netty发送心跳 channel:{} =====", ctx.channel().id().asShortText());
    }


    /**
     * 收到服务端消息
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
            logger.error("channelId:{} 报文解析失败 msg:{}", ctx.channel().id(), msg.toString());
        }
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("===== Netty异常 channel:{} =====", ctx.channel().id().asShortText(), cause);

        /*
         * 只关闭当前异常channel
         */
        ctx.close();
    }
}