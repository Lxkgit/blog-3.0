package com.blog.core.domain.netty.dto.sensor.control.param;

import com.alibaba.fastjson2.annotation.JSONField;
import com.blog.core.domain.netty.dto.sensor.control.SensorCommandCheckDto;
import com.blog.core.valication.annotation.Equal;
import com.blog.core.valication.group.InsertGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description led控制参数
 * @Author lxk
 * @CreateTime 2025-11-19
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class LEDDto extends SensorCommandCheckDto {

    /**
     * 舵机命令控制旋转角度
     */
    @Equal(value = "0,1", message = "LED控制参数（1：打开 0：关闭）错误", groups = {InsertGroup.class})
    @JSONField(name = "data")
    private Integer data;

}