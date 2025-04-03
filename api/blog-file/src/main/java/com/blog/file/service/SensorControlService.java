package com.blog.file.service;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.file.device.vo.SensorControlVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.MyPage;

import java.util.List;

/**
 * @description: 传感器控制服务类
 * @Author: lxk
 * @date 2024/2/2 10:34
 */

public interface SensorControlService {

    Integer createSensorControl(Integer userId, SensorControlVo sensorControlVo) throws ServiceException, IllegalAccessException, InstantiationException, NoSuchFieldException;

    Integer deleteSensorControl(Integer userId, List<Integer> ids);

    Integer updateSensorControl(Integer userId, SensorControlVo sensorControlVo);

    MyPage<SensorControlVo> selectSensorControlList(Integer userId, SensorControlVo sensorControlVo) throws ServiceException;

    JSONObject selectSensorControlById(Integer userId, Integer id);

    /**
     * 下发传感器控制命令
     */
    Boolean controlSensor(Integer userId, Integer id) throws ServiceException;
}
