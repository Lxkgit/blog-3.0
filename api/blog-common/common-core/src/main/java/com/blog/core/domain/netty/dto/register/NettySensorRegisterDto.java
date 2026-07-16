package com.blog.core.domain.netty.dto.register;

import lombok.Data;

/**
 * @Description 传感器设备注册类
 * @Author lxk
 * @CreateTime 2024-08-30
 */

@Data
public class NettySensorRegisterDto {

    private String sensorCode;

    private String sensorType;

}
