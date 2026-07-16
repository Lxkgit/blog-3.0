package com.blog.core.domain.socket.constant;

/**
 * @Description socket 请求类型
 * @Author lxk
 * @CreateTime 2025-06-10
 */

public class SocketPacketType {

    /**
     * 注册
     */
    public static final String REGISTER = "register";

    /**
     * 心跳
     */
    public static final String HEARTBEAT = "heartbeat";

    /**
     * 请求
     */
    public static final String REQUEST = "request";

    /**
     * 响应
     */
    public static final String RESPONSE = "response";

}
