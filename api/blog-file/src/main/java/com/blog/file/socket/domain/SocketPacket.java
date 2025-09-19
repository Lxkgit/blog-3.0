package com.blog.file.socket.domain;

import com.blog.core.domain.common.MsgHead;
import com.blog.file.netty.domain.common.NettyConstant;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.enums.NettyPacketType;
import com.blog.file.socket.domain.constant.SocketPacketType;
import lombok.Data;

import java.util.UUID;

/**
 * @Description socket消息数据包
 * @Author lxk
 * @CreateTime 2025-06-10
 */

@Data
public class SocketPacket<T> {

    /**
     * socket 消息唯一序列号
     */
    private String requestId;

    /**
     * 消息头
     */
    private MsgHead msgHead;

    /**
     * socket 请求类型
     */
    private String socketPacketType;

    /**
     * socket topic
     */
    private String topic;

    /**
     * 发送消息用户id
     */
    private Integer userId;

    /**
     * socket 消息体
     */
    private T data;

    public static <T> SocketPacket<T> buildRequest(String topic, MsgHead msgHead, T param) {
        SocketPacket<T> socketPacket = new SocketPacket<>();
        socketPacket.setRequestId(UUID.randomUUID().toString());
        socketPacket.setMsgHead(msgHead);
        socketPacket.setTopic(topic);
        socketPacket.setSocketPacketType(SocketPacketType.REQUEST);
        socketPacket.setData(param);
        return socketPacket;
    }

    public static <T> SocketPacket<T> buildRequest(String topic, T param) {
        SocketPacket<T> socketPacket = new SocketPacket<>();
        socketPacket.setRequestId(UUID.randomUUID().toString());
        socketPacket.setMsgHead(null);
        socketPacket.setTopic(topic);
        socketPacket.setSocketPacketType(SocketPacketType.REQUEST);
        socketPacket.setData(param);
        return socketPacket;
    }

    public static <T> SocketPacket<T> buildResponse(String requestId, String topic, T data) {
        SocketPacket<T> socketPacket = new SocketPacket<>();
        socketPacket.setRequestId(requestId);
        socketPacket.setTopic(topic);
        socketPacket.setSocketPacketType(SocketPacketType.RESPONSE);
        socketPacket.setData(data);
        return socketPacket;
    }
}
