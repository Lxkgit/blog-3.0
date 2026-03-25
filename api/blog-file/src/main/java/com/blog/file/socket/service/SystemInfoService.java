package com.blog.file.socket.service;

import com.blog.core.domain.file.device.entity.DeviceInfo;
import com.blog.file.mapper.DeviceInfoMapper;
import com.blog.file.socket.config.SocketService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description 系统消息服务类
 * @Author lxk
 * @CreateTime 2026-03-25
 */

@Service
public class SystemInfoService {

    private static final Logger logger = LoggerFactory.getLogger(SystemInfoService.class);

    @Resource
    private DeviceInfoMapper deviceInfoMapper;

    /**
     * 记录py脚本上报服务器设备信息
     */
    public void insertServiceInfoByPy(String json) {

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceCode("service");
        deviceInfo.setCreateTime(new Date());
        deviceInfo.setDeviceJson(json);
        deviceInfoMapper.insert(deviceInfo);
    }
}
