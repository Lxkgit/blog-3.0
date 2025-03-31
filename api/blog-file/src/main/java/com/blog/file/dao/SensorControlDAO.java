package com.blog.file.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.device.entity.SensorControl;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 传感器控制数据层
 * @Author: lxk
 * @date 2024/2/2 10:44
 */

@Mapper
public interface SensorControlDAO extends BaseMapper<SensorControl> {

    void updateSensorControlById(SensorControl sensorControl);
}
