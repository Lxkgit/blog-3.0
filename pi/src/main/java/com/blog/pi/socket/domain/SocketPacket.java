package com.blog.pi.socket.domain;

import com.blog.pi.domain.common.MsgHead;
import com.blog.pi.socket.domain.constant.SocketPacketType;
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
        socketPacket.setTopic(topic);
        socketPacket.setMsgHead(msgHead);
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
