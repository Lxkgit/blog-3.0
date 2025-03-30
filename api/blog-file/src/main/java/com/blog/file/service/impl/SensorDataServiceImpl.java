package com.blog.file.service.impl;


/**
 * @author lxk
 * @description 传感器数据服务类
 * @date 2024/02/05
 */

@Service
public class SensorDataServiceImpl implements SensorDataService {

    @Resource
    private SensorDataDAO sensorDataDAO;

    @Resource
    private SensorDAO sensorDAO;

    /**
     * 保存传感器上报数据
     *
     * @param sensorData
     * @return
     */
    @Override
    public Integer saveSensorData(SensorData sensorData) {
        sensorData.setCreateTime(new Date());
        return sensorDataDAO.insert(sensorData);
    }

    /**
     * 查询数据传感器上报数据
     *
     * @param userId
     * @param sensorDataVoParam
     * @return
     */
    @Override
    public MyPage<SensorDataVo> selectSensorDataList(Integer userId, SensorDataVo sensorDataVoParam) {

        Sensor sensor = sensorDAO.selectById(sensorDataVoParam.getSensorId());
        LambdaQueryWrapper<SensorData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensorData::getDeviceCode, sensor.getDeviceCode());
        wrapper.eq(SensorData::getChipCode, sensor.getChipCode());
        wrapper.eq(SensorData::getSensorCode, sensor.getSensorCode());


        PageHelper.startPage(sensorDataVoParam.getPageNum(), sensorDataVoParam.getPageSize());
        Page<SensorData> sensorDataPage = (Page<SensorData>) sensorDataDAO.selectList(wrapper);

        List<SensorDataVo> sensorDataVoList = new ArrayList<>();
        for (SensorData sensorData : sensorDataPage) {
            SensorDataVo sensorDataVo = new SensorDataVo();
            BeanUtils.copyProperties(sensorData, sensorDataVo);
            sensorDataVoList.add(sensorDataVo);
        }

        return MyPageUtils.pageUtil(sensorDataVoList, sensorDataPage.getPageNum(), sensorDataPage.getPageSize(), (int) sensorDataPage.getTotal());

    }
}
