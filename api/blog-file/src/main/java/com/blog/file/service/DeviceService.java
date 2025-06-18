package com.blog.file.service;

import com.blog.core.domain.file.device.entity.Device;
import com.blog.core.domain.file.device.vo.DeviceInfoVo;
import com.blog.core.domain.file.device.vo.DeviceVo;
import com.blog.core.exception.ServiceException;

import java.util.List;

/**
 * @description: 下级设备服务类
 * @Author: lxk
 * @date 2024/1/29 13:49
 */

public interface DeviceService {

    Integer addDevice(DeviceVo deviceVo) throws ServiceException;

    Integer updateDevice(DeviceVo deviceVo) throws ServiceException;

    Integer deleteDevice(String ids) throws ServiceException;

    List<Device> selectDeviceList() throws ServiceException;

    DeviceVo selectDeviceById(Integer id) throws ServiceException;

    List<DeviceInfoVo> selectDeviceInfoById(Integer id);

    void getDeviceStatus();
}
