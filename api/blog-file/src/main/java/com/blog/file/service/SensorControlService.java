package com.blog.file.service;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.file.device.vo.SensorControlVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.ResultPage;

import java.util.List;

/**
 * @description: 传感器控制服务类
 * @Author: lxk
 * @date 2024/2/2 10:34
 */

public interface SensorControlService {

    Integer createSensorControl(SensorControlVo sensorControlVo);

    Integer deleteSensorControl(List<Integer> ids);

    Integer updateSensorControl(SensorControlVo sensorControlVo);

    ResultPage<SensorControlVo> selectSensorControlList(SensorControlVo sensorControlVo) throws ServiceException;

    JSONObject selectSensorControlById(Integer id);

    /**
     * 下发传感器控制命令
     */
    Boolean controlSensor(Integer id) throws ServiceException;
}
