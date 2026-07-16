package com.blog.core.domain.netty.dto;


import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.netty.head.NettyMsgHead;
import com.blog.core.domain.netty.common.NettyConstant;
import com.blog.core.domain.netty.enums.NettyPacketType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author
 * 自定义Netty数据包
 */

@Data
public class NettyPacket<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 3450384644298931566L;

    public static Map<String, NettyPacket<Object>> MESSAGE_QUEUE = new ConcurrentHashMap<>();

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
     * 构建netty请求消息
     *
     * @param topic
     * @param param
     * @param <T>
     * @return
     */
    public static <T> NettyPacket<T> buildRequest(NettyPacketType nettyPacketType, String topic, T param, String registerCode) {
        NettyPacket<T> nettyPacket = new NettyPacket<>();
        MsgHead msgHead = buildNettyMsgHead(UUID.randomUUID().toString(), topic, nettyPacketType, registerCode);
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
        if (msgHead != null && msgHead.getNettyMsgHead() != null) {
            msgHead.getNettyMsgHead().setNettyPacketType(NettyPacketType.RESPONSE.getValue());
            msgHead.getNettyMsgHead().setRegisterCode("1:2ecfb95116de4967afe7710e11ac00b4");
        }
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
        nettyMsgHead.setRegisterCode(NettyConstant.NETTY_DEVICE_CODE);
        nettyMsgHead.setNettyPacketType(response.getValue());
        msgHead.setNettyMsgHead(nettyMsgHead);
        return msgHead;
    }

    /**
     * 构建netty请求头消息
     *
     * @param requestId
     * @param topic
     * @param response
     * @return
     */
    private static MsgHead buildNettyMsgHead(String requestId, String topic, NettyPacketType response, String registerCode) {
        MsgHead msgHead = new MsgHead();
        NettyMsgHead nettyMsgHead = new NettyMsgHead();
        nettyMsgHead.setRegisterCode(registerCode);
        nettyMsgHead.setRequestId(requestId);
        nettyMsgHead.setTopic(topic);
        nettyMsgHead.setNettyPacketType(response.getValue());
        msgHead.setNettyMsgHead(nettyMsgHead);
        return msgHead;
    }

    public void setMsgHead(MsgHead msgHead) {
        if (msgHead == null) {
            return;
        }
        if (this.msgHead == null) {
            this.msgHead = msgHead;
            return;
        }

        // 只在 this.msgHead 的属性为 null 时，才从 msgHead 复制值
        try {
            for (Field field : this.msgHead.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object targetValue = field.get(this.msgHead);
                Object sourceValue = field.get(msgHead);
                if (targetValue == null && sourceValue != null) {
                    field.set(this.msgHead, sourceValue);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("设置 msgHead 属性失败", e);
        }
    }


    public static void request(String requestId, NettyPacket<Object> nettyResponse) {
        MESSAGE_QUEUE.put(requestId, nettyResponse);
    }

    public static void response(String requestId) {
        MESSAGE_QUEUE.remove(requestId);
    }

}
