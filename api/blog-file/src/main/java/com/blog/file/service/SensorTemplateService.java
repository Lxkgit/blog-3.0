package com.blog.file.service;

import com.blog.core.domain.file.device.dto.SensorTemplateDTO;
import com.blog.core.domain.file.device.vo.SensorTemplateVO;
import com.blog.core.exception.ServiceException;

import java.util.List;

/**
 * @author lxk
 * @description 传感器模板服务类接口
 * @date 2024/09/29
 */

public interface SensorTemplateService {

    List<SensorTemplateVO> selectSensorTemplateByChipOrSensorId(SensorTemplateDTO sensorTemplateDTO) throws ServiceException;
}
