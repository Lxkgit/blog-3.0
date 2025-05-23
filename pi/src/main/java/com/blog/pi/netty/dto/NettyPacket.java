package com.blog.pi.netty.dto;


import com.blog.pi.config.PiSystemConfig;
import com.blog.pi.netty.enums.NettyPacketType;
import com.blog.pi.utils.MyUUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;


/**
 * @author Andon
 * 2022/7/27
 * <p>
 * 自定义Netty数据包
 */
@Data
@Slf4j
public class NettyPacket<T> implements Serializable {

    private static final long serialVersionUID = 410568910242170750L;

    /**
     *  netty 消息唯一序列号
     */
    private String requestId;

    /**
     * netty 请求类型
     */
    private String nettyPacketType;

    /**
     * netty 消息Topic
     */
    private String topic;

    /**
     * 消息所属用户
     */
    private String username;

    /**
     * netty注册id
     */
    private String registerCode;

    /**
     * netty 消息内容
     */
    private T data;

    public static <T> NettyPacket<T> buildRequest(T param) {
        NettyPacket<T> nettyPacket = new NettyPacket<>();
        nettyPacket.setRequestId(MyUUID.getRandomString());
        nettyPacket.setUsername("gszero");
        nettyPacket.setRegisterCode("1:2ecfb95116de4967afe7710e11ac00b4");
        nettyPacket.setNettyPacketType(NettyPacketType.REQUEST.getValue());
        nettyPacket.setData(param);
        return nettyPacket;
    }

    public static <T> NettyPacket<T> buildResponse(String requestId, T data) {
        NettyPacket<T> nettyPacket = new NettyPacket<>();
        nettyPacket.setRequestId(requestId);
        nettyPacket.setUsername("gszero");
        nettyPacket.setRegisterCode("1:2ecfb95116de4967afe7710e11ac00b4");
        nettyPacket.setNettyPacketType(NettyPacketType.RESPONSE.getValue());
        nettyPacket.setData(data);
        return nettyPacket;
    }
}
