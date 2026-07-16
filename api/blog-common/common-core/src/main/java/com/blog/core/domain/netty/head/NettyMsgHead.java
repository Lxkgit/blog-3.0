package com.blog.core.domain.netty.head;

import lombok.Data;

/**
 * @Description netty消息头
 * @Author lxk
 * @CreateTime 2025-10-23
 */

@Data
public class NettyMsgHead {

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
     * netty注册id
     */
    private String registerCode;
}
