package com.blog.file.service.impl;


import com.blog.core.domain.file.device.entity.SensorType;
import com.blog.file.mapper.SensorTypeMapper;
import com.blog.file.service.SensorTypeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: 传感器类型服务类
 * @Author: lxk
 * @date 2024/2/22 14:55
 */

@Slf4j
@Service
public class SensorTypeServiceImpl implements SensorTypeService {

    @Resource
    private SensorTypeMapper sensorTypeDAO;

    public List<SensorType> selectSensorTypeList() {
        return sensorTypeDAO.selectList(null);
    }
}
