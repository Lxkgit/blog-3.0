package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.device.entity.SensorType;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lxk
 * @description 传感器类型数据层
 * @date 2024/02/10
 */

@Mapper
public interface SensorTypeMapper extends BaseMapper<SensorType> {

}
