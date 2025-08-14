package com.blog.file.service;

import com.blog.core.domain.file.device.vo.SensorVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.ResultPage;

/**
 * @description: 传感器服务类
 * @Author: lxk
 * @date 2024/1/30 20:08
 */

public interface SensorService {


    Integer addSensor(SensorVo sensorVo) throws ServiceException;

    Integer deleteSensors(String ids);

    Integer updateSensor(SensorVo sensorVo) throws ServiceException;

    ResultPage<SensorVo> selectSensorList(SensorVo sensorVo);

    SensorVo selectSensorId(Integer id);

}
