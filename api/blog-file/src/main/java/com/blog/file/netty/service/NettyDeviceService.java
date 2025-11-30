package com.blog.file.netty.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.core.domain.file.device.entity.*;
import com.blog.file.mapper.*;
import com.blog.file.netty.domain.dto.register.NettyChipRegisterDto;
import com.blog.file.netty.domain.dto.register.NettySensorRegisterDto;
import com.blog.file.netty.domain.dto.sensor.receive.SensorDataDto;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @description: 处理Netty收到的传感器数据服务
 * @Author: lxk
 * @date 2024/3/11 10:52
 */

@Service
public class NettyDeviceService {

    @Resource
    private NettyServer nettyServer;

    @Resource
    private DeviceMapper deviceDAO;

    @Resource
    private ChipMapper chipDAO;

    @Resource
    private SensorMapper sensorDAO;

    @Resource
    private SensorDataMapper sensorDataDAO;

    @Resource
    private DeviceChipMapper deviceChipDAO;

    @Resource
    private ChipSensorMapper chipSensorDAO;

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    /**
     * 传感器设备主动注册
     * @param data
     * @param deviceCode
     */
    public void chipAndSensorRegister(String data, String deviceCode, Integer userId) {
        NettyChipRegisterDto nettyChipRegisterDto = JSONObject.parseObject(data, NettyChipRegisterDto.class);

        QueryWrapper<DeviceChip> chipQueryWrapper = new QueryWrapper<>();
        chipQueryWrapper.eq("device_code", deviceCode).eq("chip_code", nettyChipRegisterDto.getChipCode());
        DeviceChip selectChip = deviceChipDAO.selectOne(chipQueryWrapper);
        if (selectChip != null) {
            Chip chip = new Chip();
            chip.setUserId(userId);
            chip.setDeviceCode(deviceCode);
            chip.setChipCode(nettyChipRegisterDto.getChipCode());
            chip.setChipName(nettyChipRegisterDto.getChipName());
            chip.setChipStatus(1);
            chip.setChipType(nettyChipRegisterDto.getChipType());
            chip.setUpdateTime(new Date());

            DeviceChip deviceChip = new DeviceChip();
            deviceChip.setId(selectChip.getId());
            deviceChip.setUpdateTime(new Date());

            if (selectChip.getCodeStatus() == 0) {
                chip.setCreateTime(new Date());
                chipDAO.insert(chip);
                deviceChip.setCodeStatus(1);
            } else {
                QueryWrapper<Chip> wrapper = new QueryWrapper<>();
                wrapper.eq("device_code", deviceCode).eq("chip_code", nettyChipRegisterDto.getChipCode());
                chipDAO.update(chip, wrapper);
            }
            deviceChipDAO.updateById(deviceChip);

            for (NettySensorRegisterDto nettySensorRegisterDto : nettyChipRegisterDto.getSensorList()) {
                QueryWrapper<ChipSensor> sensorQueryWrapper = new QueryWrapper<>();
                sensorQueryWrapper.eq("chip_code", selectChip.getChipCode()).eq("sensor_code", nettySensorRegisterDto.getSensorCode());
                ChipSensor selectSensor = chipSensorDAO.selectOne(sensorQueryWrapper);
                if (selectSensor != null) {
                    Sensor sensor = new Sensor();
                    sensor.setUserId(userId);
                    sensor.setDeviceCode(deviceCode);
                    sensor.setChipCode(nettyChipRegisterDto.getChipCode());
                    sensor.setSensorCode(nettySensorRegisterDto.getSensorCode());
                    sensor.setSensorStatus(1);
                    sensor.setSensorType(nettySensorRegisterDto.getSensorType());
                    sensor.setUpdateTime(new Date());

                    ChipSensor chipSensor = new ChipSensor();
                    chipSensor.setId(selectSensor.getId());
                    chipSensor.setUpdateTime(new Date());
                    if (selectSensor.getCodeStatus() == 0) {
                        sensor.setCreateTime(new Date());
                        sensorDAO.insert(sensor);

                        chipSensor.setCodeStatus(1);
                    } else {
                        QueryWrapper<Sensor> wrapper = new QueryWrapper<>();
                        wrapper.eq("chip_code", nettyChipRegisterDto.getChipCode())
                                .eq("sensor_code", nettySensorRegisterDto.getSensorCode());
                        sensorDAO.update(sensor, wrapper);
                    }
                    chipSensorDAO.updateById(chipSensor);
                }
            }
        }
    }

    /**
     * 处理接收到的传感器数据
     * @param data JSON 格式数据
     * @param deviceCode 设备编码
     */
    public void receiveSensorData(String data, String deviceCode) {
        SensorDataDto sensorDataDto = JSONObject.parseObject(data, SensorDataDto.class);

        // 传感器数据
        for (SensorDataDto.ReceiveData receiveData : sensorDataDto.getDataList()) {
            SensorData sensorData = new SensorData();
            sensorData.setDeviceCode(deviceCode);
            sensorData.setChipCode(sensorDataDto.getChipCode());
            sensorData.setMsgCode(sensorDataDto.getMsgCode());
            sensorData.setMsgCount(sensorDataDto.getMsgCount());
            sensorData.setCreateTime(new Date());
            sensorData.setSensorCode(receiveData.getSensorCode());
            sensorData.setSensorData(receiveData.getSensorData());

            sensorDataDAO.insert(sensorData);
        }
    }

    public void SensorControl() {

    }

    public void deviceInfo(String data, String deviceCode, Integer userId) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceCode(deviceCode);
        deviceInfo.setUserId(userId);
        deviceInfo.setDeviceJson(data);
        deviceInfo.setCreateTime(new Date());
        deviceInfoMapper.insert(deviceInfo);
    }
}
