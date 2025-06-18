package com.blog.file.netty.domain.dto.status;

import lombok.Data;

/**
 * @Description 设备socket在线状态类
 * @Author lxk
 * @CreateTime 2025-06-18
 */

@Data
public class NettySocketStatusDto {

    /**
     * socket 在线状态
     * 0 离线
     * 1 在线
     */
    private Integer socketStatus;
}
