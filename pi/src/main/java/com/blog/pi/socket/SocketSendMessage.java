package com.blog.pi.socket;

import lombok.Data;

/**
 * @description:
 * @Author: lxk
 * @date 2023/7/5 14:24
 */

@Data
public class SocketSendMessage<T> {

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
    private T message;

}
