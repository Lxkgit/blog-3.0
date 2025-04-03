package com.blog.file.service;

import com.blog.core.domain.file.device.vo.SensorVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.MyPage;

/**
 * @description: 传感器服务类
 * @Author: lxk
 * @date 2024/1/30 20:08
 */

public interface SensorService {


    Integer addSensor(Integer userId, SensorVo sensorVo) throws ServiceException;

    Integer deleteSensors(Integer userId, String ids);

    Integer updateSensor(Integer userId, SensorVo sensorVo) throws ServiceException;

    MyPage<SensorVo> selectSensorList(Integer userId, SensorVo sensorVo);

    SensorVo selectSensorId(Integer userId, Integer id);

}
