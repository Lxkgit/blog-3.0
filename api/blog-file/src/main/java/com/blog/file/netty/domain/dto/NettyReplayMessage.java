package com.blog.file.netty.domain.dto;

import lombok.Data;

import java.util.Date;

/**
 * 消息重发配置
 */
@Data
public class NettyReplayMessage {

    /**
     * 消息重发类型
     * 1 限制重发次数
     * 2 限制重发时间
     */
    private Integer retryType;

    /**
     * 消息重发次数
     * 指定消息重发次数 0 为无限制
     */
    private Integer limitCount;

    /**
     * 消息有效时间 单位：minute
     * 超过有效时间的消息被丢弃
     */
    private Integer effectiveTime;

    /**
     * netty 发送消息
     */
    private String message;

    /**
     * 消息首次发送时间
     */
    private Date firstSendTime;

    /**
     * 消息最近发送时间
     */
    private Date lastSendTime;

    /**
     * 消息已重发次数
     */
    private Integer tryCount;

}
