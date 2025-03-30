package com.blog.file.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.entity.SensorData;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 传感器数据
 * @Author: lxk
 * @date 2024/2/1 14:06
 */

@Mapper
public interface SensorDataDAO extends BaseMapper<SensorData> {
}
