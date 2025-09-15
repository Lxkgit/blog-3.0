package com.blog.pi.netty.service.vo;

import lombok.Data;

/**
 * @description: 传感器命令基础类
 * @Author: lxk
 * @date 2024/3/29 20:00
 */

@Data
public class SensorCommandVo {

    /**
     * 单片机类型
     */
    private String chipType;

    /**
     * 单片机编码
     */
    private String chipCode;

    /**
     * 传感器类型
     */
    private String sensorType;

    /**
     * 传感器编码
     */
    private String sensorCode;

    /**
     * 命令延时 多条组合命令延时使用,分隔
     */
    private Integer controlIntervalTime;
}
