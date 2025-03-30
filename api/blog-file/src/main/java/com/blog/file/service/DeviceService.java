package com.blog.file.service;

import com.blog.core.domain.file.entity.Device;
import com.blog.core.domain.file.vo.DeviceHeartbeatVo;
import com.blog.core.domain.file.vo.DeviceVo;
import com.blog.core.exception.ValidException;

import java.util.List;

/**
 * @description: 下级设备服务类
 * @Author: lxk
 * @date 2024/1/29 13:49
 */

public interface DeviceService {

    Integer addDevice(Integer userId, DeviceVo deviceVo) throws ValidException;

    Integer updateDevice(Integer userId, DeviceVo deviceVo) throws ValidException;

    Integer deleteDevice(Integer userId, String ids) throws ValidException;

    List<Device> selectDeviceList(Integer userId) throws ValidException;

    DeviceVo selectDeviceById(Integer userId, Integer id) throws ValidException;

    List<DeviceHeartbeatVo> selectDeviceInfoById(Integer userId, Integer id);
}
