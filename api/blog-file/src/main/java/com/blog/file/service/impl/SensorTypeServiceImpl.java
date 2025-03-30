package com.blog.file.service.impl;


/**
 * @description: 传感器类型服务类
 * @Author: lxk
 * @date 2024/2/22 14:55
 */

@Slf4j
@Service
public class SensorTypeServiceImpl implements SensorTypeService {

    @Resource
    private SensorTypeDAO sensorTypeDAO;

    public List<SensorType> selectSensorTypeList() {
        return sensorTypeDAO.selectList(null);
    }
}
