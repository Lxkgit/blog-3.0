package com.blog.file.service;

import com.blog.core.domain.file.device.entity.Device;
import com.blog.core.domain.file.device.vo.DeviceHeartbeatVo;
import com.blog.core.domain.file.device.vo.DeviceVo;
import com.blog.core.exception.ServiceException;

import java.util.List;

/**
 * @description: 下级设备服务类
 * @Author: lxk
 * @date 2024/1/29 13:49
 */

public interface DeviceService {

    Integer addDevice(Integer userId, DeviceVo deviceVo) throws ServiceException;

    Integer updateDevice(Integer userId, DeviceVo deviceVo) throws ServiceException;

    Integer deleteDevice(Integer userId, String ids) throws ServiceException;

    List<Device> selectDeviceList(Integer userId) throws ServiceException;

    DeviceVo selectDeviceById(Integer userId, Integer id) throws ServiceException;

    List<DeviceHeartbeatVo> selectDeviceInfoById(Integer userId, Integer id);
}
