package com.blog.file.service;

import com.blog.core.domain.file.entity.SensorType;

import java.util.List;

/**
 * @description: 传感器类型服务类
 * @Author: lxk
 * @date 2024/2/22 14:54
 */

public interface SensorTypeService {

    List<SensorType> selectSensorTypeList();
}
