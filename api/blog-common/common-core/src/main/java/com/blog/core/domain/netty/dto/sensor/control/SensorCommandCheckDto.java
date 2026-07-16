package com.blog.core.domain.netty.dto.sensor.control;

import com.alibaba.fastjson2.annotation.JSONField;
import com.blog.core.valication.group.InsertGroup;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * @description: 传感器下发命令参数校验基础类
 * @Author: lxk
 * @date 2024/2/2 11:04
 */


@Getter
@Setter
public class SensorCommandCheckDto {


    @Max(value = 10000,message="命令等待延时范围是0-10000ms",groups={InsertGroup.class})
    @Min(value = 0,message="命令等待延时范围是0-10000ms",groups={InsertGroup.class})
    @JSONField(name = "delay")
    private Integer delay;

    /**
     * 被控制传感器编码
     */
    private String sensorCode;

    /**
     * 传感器命令执行顺序
     */
    private Integer idx;

    /**
     * 传感器id
     */
    private Integer id;

    /**
     * 传感器类型
     */
    private String sensorType;
}
