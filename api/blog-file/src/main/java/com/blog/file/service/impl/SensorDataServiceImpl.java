package com.blog.file.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.device.entity.Sensor;
import com.blog.core.domain.file.device.entity.SensorData;
import com.blog.core.domain.file.device.vo.SensorDataVo;
import com.blog.core.result.ResultPage;
import com.blog.core.result.ResultPageUtils;
import com.blog.file.mapper.SensorMapper;
import com.blog.file.mapper.SensorDataMapper;
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
    private SensorDataMapper sensorDataMapper;

    @Resource
    private SensorMapper sensorMapper;

    /**
     * 保存传感器上报数据
     *
     * @param sensorData
     * @return
     */
    @Override
    public Integer saveSensorData(SensorData sensorData) {
        sensorData.setCreateTime(new Date());
        return sensorDataMapper.insert(sensorData);
    }

    /**
     * 查询数据传感器上报数据
     *
     * @param sensorDataVoParam
     * @return
     */
    @Override
    public ResultPage<SensorDataVo> selectSensorDataList(SensorDataVo sensorDataVoParam) {

        Sensor sensor = sensorMapper.selectById(sensorDataVoParam.getSensorId());
        LambdaQueryWrapper<SensorData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensorData::getDeviceCode, sensor.getDeviceCode());
        wrapper.eq(SensorData::getChipCode, sensor.getChipCode());
        wrapper.eq(SensorData::getSensorCode, sensor.getSensorCode());
        wrapper.orderByDesc(SensorData::getCreateTime);

        PageHelper.startPage(sensorDataVoParam.getPageNum(), sensorDataVoParam.getPageSize());
        Page<SensorData> sensorDataPage = (Page<SensorData>) sensorDataMapper.selectList(wrapper);

        List<SensorDataVo> sensorDataVoList = new ArrayList<>();
        for (SensorData sensorData : sensorDataPage) {
            SensorDataVo sensorDataVo = new SensorDataVo();
            BeanUtils.copyProperties(sensorData, sensorDataVo);
            sensorDataVoList.add(sensorDataVo);
        }

        return ResultPageUtils.pageUtil(sensorDataVoList, sensorDataPage.getPageNum(), sensorDataPage.getPageSize(), (int) sensorDataPage.getTotal());

    }
}
