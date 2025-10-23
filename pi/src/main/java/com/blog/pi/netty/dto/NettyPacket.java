package com.blog.pi.netty.dto;


import com.blog.pi.domain.common.MsgHead;
import com.blog.pi.domain.common.NettyMsgHead;
import com.blog.pi.netty.enums.NettyPacketType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.UUID;


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
     * 消息头
     */
    private MsgHead msgHead;

    /**
     * netty 消息内容
     */
    private T data;

    /**
     * 构建netty请求消息
     *
     * @param topic
     * @param param
     * @param <T>
     * @return
     */
    public static <T> NettyPacket<T> buildRequest(String topic, T param) {
        NettyPacket<T> nettyPacket = new NettyPacket<>();
        MsgHead msgHead = buildNettyMsgHead(UUID.randomUUID().toString(), topic, NettyPacketType.REQUEST);
        nettyPacket.setMsgHead(msgHead);
        nettyPacket.setData(param);
        return nettyPacket;
    }

    /**
     * 构建netty响应消息
     *
     * @param requestId
     * @param topic
     * @param data
     * @param <T>
     * @return
     */
    public static <T> NettyPacket<T> buildResponse(String requestId, String topic, T data) {
        NettyPacket<T> nettyPacket = new NettyPacket<>();
        MsgHead msgHead = buildNettyMsgHead(requestId, topic, NettyPacketType.RESPONSE);
        nettyPacket.setMsgHead(msgHead);
        nettyPacket.setData(data);
        return nettyPacket;
    }

    /**
     * 构建netty响应消息
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> NettyPacket<T> buildResponse(MsgHead msgHead, T data) {
        NettyPacket<T> nettyPacket = new NettyPacket<>();
        nettyPacket.setMsgHead(msgHead);
        nettyPacket.setData(data);
        return nettyPacket;
    }


    /**
     * 构建netty请求头消息
     *
     * @param requestId
     * @param topic
     * @param response
     * @return
     */
    private static MsgHead buildNettyMsgHead(String requestId, String topic, NettyPacketType response) {
        MsgHead msgHead = new MsgHead();
        NettyMsgHead nettyMsgHead = new NettyMsgHead();
        nettyMsgHead.setRequestId(requestId);
        nettyMsgHead.setTopic(topic);
        nettyMsgHead.setRegisterCode("1:2ecfb95116de4967afe7710e11ac00b4");
        nettyMsgHead.setNettyPacketType(response.getValue());
        msgHead.setNettyMsgHead(nettyMsgHead);
        return msgHead;
    }

    public void setMsgHead(MsgHead msgHead) {
        if (this.msgHead == null) {
            this.msgHead = msgHead;
        } else {
            BeanUtils.copyProperties(msgHead, this.msgHead);
        }
    }
}
