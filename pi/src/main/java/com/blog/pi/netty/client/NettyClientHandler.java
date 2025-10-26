package com.blog.pi.netty.client;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.blog.pi.mqtt.http.ChipStatusService;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.dto.heart.NettyHeartBeatDto;
import com.blog.pi.netty.dto.register.NettyRegisterDto;
import com.blog.pi.netty.enums.HeartBeatType;
import com.blog.pi.netty.enums.NettyPacketType;
import com.blog.pi.netty.enums.NettyTopicEnum;
import com.blog.pi.netty.event.NettyPacketEvent;
import com.blog.pi.netty.service.DeviceInfoService;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @Author: lxk
 * @date 2024/1/6 15:52
 */
@Slf4j
@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class NettyClientHandler extends ChannelDuplexHandler {

    @Lazy
    @Resource
    private NettyClient nettyClient;

    @Resource
    private DeviceInfoService deviceInfoService;

    @Resource
    private ChipStatusService chipStatusService;

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 客户端连接到服务端后调用
     * 可在此次发送客户端注册
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        // 组装netty注册消息类
        NettyRegisterDto nettyRegisterDto = new NettyRegisterDto();
        nettyRegisterDto.setDeviceName("树莓派");
        nettyRegisterDto.setMemo("这个是设备备注信息");
        deviceInfoService.setRegisterMsg(nettyRegisterDto);

        // 发送注册消息
        NettyPacket<NettyRegisterDto> nettyRequest = NettyPacket.buildRequest(NettyPacketType.REGISTER, NettyPacketType.REGISTER.getValue(), nettyRegisterDto);
        String nettyRegister = JSONObject.toJSONString(nettyRequest);
        ctx.writeAndFlush(nettyRegister);
    }

    /**
     * 关闭连接时
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn("netty service close");
        reconnect(ctx);
    }

    /**
     * 心跳处理，每30秒发送一次心跳请求
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                NettyHeartBeatDto nettyHeartBeat = new NettyHeartBeatDto();
                nettyHeartBeat.setHeartBeat(new Date());
                nettyHeartBeat.setType(HeartBeatType.SERVICE.getType());

//                nettyHeartBeat.setClientIds(chipStatusService.getMqttClientId(true));

                // 向服务端发送心跳包
                NettyPacket<NettyHeartBeatDto> nettyRequest = NettyPacket.buildRequest(NettyPacketType.HEARTBEAT, NettyPacketType.HEARTBEAT.getValue(), nettyHeartBeat);
                nettyRequest.getMsgHead().getNettyMsgHead().setNettyPacketType(NettyPacketType.HEARTBEAT.getValue());

                // 发送心跳消息，并在发送失败时关闭该连接
                ctx.writeAndFlush(JSONObject.toJSONString(nettyRequest));
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
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
            log.error("channelId:{} 报文解析失败 msg:{}", ctx.channel().id(), msg.toString());
//            NettyPacket<String> nettyResponse = NettyPacket.buildRequest("报文解析失败: " + msg);
//            nettyResponse.setTopic(NettyTopicEnum.MSG_ERROR_RESPONSE.getTopic());
//            ctx.writeAndFlush(JSONObject.toJSONString(nettyResponse));
        }
    }

    /**
     * 当连接发生异常时触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.info(cause.getMessage());
        cause.getStackTrace();
        // 当出现异常就关闭连接
        ctx.close();
    }

    private void reconnect(ChannelHandlerContext ctx) {
        log.info("netty reconnect after 30 seconds");
        ctx.channel().eventLoop().schedule(() -> nettyClient.run(), 30, TimeUnit.SECONDS);
    }
}
