package com.blog.core.domain.file.device.vo;

import com.blog.core.domain.file.device.dto.NettyHeartbeatDto;
import com.blog.core.domain.file.device.entity.DeviceHeartbeat;
import lombok.Getter;
import lombok.Setter;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2024-09-23
 */

@Getter
@Setter
public class DeviceHeartbeatVo extends DeviceHeartbeat {

    private NettyHeartbeatDto nettyHeartbeatDto;
}
