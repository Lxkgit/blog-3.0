package com.blog.file.netty.domain.enums;

import lombok.Data;

@Data
public class NettyTopic {

    /**
     * 文件同步
     */
    public static final String BLOG_FILE_SYNC = "blog_file_sync";

    /**
     * 传感器控制
     */
    public static final String BLOG_SENSOR_CONTROL = "blog_sensor_control";

    /**
     * 个人设备 netty 与 socket 连接状态
     */
    public static final String BLOG_NETTY_SOCKET_STATUS = "blog_netty_socket_status";
}
