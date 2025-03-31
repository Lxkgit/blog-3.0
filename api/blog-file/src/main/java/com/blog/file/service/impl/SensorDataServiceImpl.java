package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.device.entity.Sensor;
import com.blog.core.domain.file.device.entity.SensorData;
import com.blog.core.domain.file.device.vo.SensorDataVo;
import com.blog.core.result.MyPage;
import com.blog.core.result.MyPageUtils;
import com.blog.file.dao.SensorDAO;
import com.blog.file.dao.SensorDataDAO;
import com.blog.file.service.SensorDataService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
