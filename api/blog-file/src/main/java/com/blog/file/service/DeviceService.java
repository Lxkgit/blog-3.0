package com.blog.file.service;

import com.blog.core.domain.file.device.entity.Device;
import com.blog.core.domain.file.device.vo.DeviceVo;
import com.blog.core.exception.ServiceException;

import java.util.List;
import java.util.Map;

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

    void getDeviceStatus();

    Map<String, Object> selectDeviceInfoByDevice(DeviceVo deviceVo);
}
