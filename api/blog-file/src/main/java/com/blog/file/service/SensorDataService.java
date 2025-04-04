package com.blog.file.service;

import com.blog.core.domain.file.device.entity.SensorData;
import com.blog.core.domain.file.device.vo.SensorDataVo;
import com.blog.core.result.MyPage;

/**
 * @author lxk
 * @description 传感器数据服务类
 * @date 2024/02/05
 */

public interface SensorDataService {

    Integer saveSensorData(SensorData sensorData);

    MyPage<SensorDataVo> selectSensorDataList(SensorDataVo sensorDataVo);
}
