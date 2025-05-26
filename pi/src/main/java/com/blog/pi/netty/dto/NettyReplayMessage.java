package com.blog.pi.netty.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class NettyReplayMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息发送时间
     */
    private Date sendTime;

    /**
     * 消息重发次数
     */
    private Integer tryTime;

    /**
     * netty 发送消息
     */
    private String message;

    public NettyReplayMessage() {
    }

    public NettyReplayMessage(String message) {
        this.sendTime = new Date();
        this.tryTime = 0;
        this.message = message;
    }
}
