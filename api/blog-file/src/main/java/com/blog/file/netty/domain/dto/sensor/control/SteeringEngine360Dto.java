package com.blog.file.netty.domain.dto.sensor.control;


import com.alibaba.fastjson2.annotation.JSONField;
import com.blog.core.valication.group.InsertGroup;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: 舵机命令下发参数
 * @Author: lxk
 * @date 2024/2/2 11:06
 */


@Data
@EqualsAndHashCode(callSuper = true)
public class SteeringEngine360Dto extends SensorCommandCheckDto {

    /**
     * 舵机命令控制旋转角度
     */
    @Max(value = 360,message="舵机参数范围为0-360",groups={InsertGroup.class})
    @Min(value = 0,message="舵机参数范围为0-360",groups={InsertGroup.class})
    @JSONField(name = "data")
    private Integer data;

}
