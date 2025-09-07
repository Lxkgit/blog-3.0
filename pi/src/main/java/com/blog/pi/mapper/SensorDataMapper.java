package com.blog.pi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.pi.domain.entity.SensorData;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 传感器数据接口
 * @Author: lxk
 * @date 2024/1/9 11:01
 */

@Mapper
public interface SensorDataMapper extends BaseMapper<SensorData> {
}
