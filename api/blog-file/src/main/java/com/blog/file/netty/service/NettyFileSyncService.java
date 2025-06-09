package com.blog.file.netty.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.device.entity.UserDevice;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.minio.MinioService;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.domain.dto.sensor.receive.SensorDataDto;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: Netty文件同步业务
 * @Author: lxk
 * @date 2024/3/11 11:41
 */

@Service
public class NettyFileSyncService {

    private static final Logger logger = LoggerFactory.getLogger(NettyFileSyncService.class);

    @Resource
    private MinioService minioService;

    @Resource
    private NettyServer nettyServer;

    @Resource
    private UserDeviceMapper userDeviceMapper;

    /**
     * netty消息发送文件同步到服务器
     * @param nettySyncFileDto 同步文件参数
     */
    public void syncFileSend(NettySyncFileDto nettySyncFileDto) {
        Integer userId = SecurityUtil.getLoginUser().getId();

        // 获取用户默认同步数据设备
        UserDevice userDevice = new UserDevice();
        userDevice.setUserId(userId);
        LambdaQueryWrapper<UserDevice> userDeviceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userDeviceLambdaQueryWrapper.eq(UserDevice::getUserId, userId);
        List<UserDevice> deviceList = userDeviceMapper.selectList(userDeviceLambdaQueryWrapper);

        if (CollectionUtils.isNotEmpty(deviceList)) {
            UserDevice device = deviceList.get(0);
            String registerId = device.getDeviceCode();

            nettyServer.channelWriteByRegisterId(registerId, JSON.toJSONString(nettySyncFileDto), true);
        }
    }

    /**
     * netty响应消息处理同步到服务器
     */
    public void syncFileReceive(String data, String deviceCode, Integer userId) {
        NettySyncFileDto nettySyncFileDto = JSONObject.parseObject(data, NettySyncFileDto.class);
        if (nettySyncFileDto.getSyncType().equals(1)) {

        } else if (nettySyncFileDto.getSyncType().equals(2)) {
            if (nettySyncFileDto.getResultType().equals(2)) {
                List<String> fileNameList = nettySyncFileDto.getFileNameList();
                if (CollectionUtils.isNotEmpty(fileNameList)) {
                    for (String fileName : fileNameList) {
                        minioService.importFile(nettySyncFileDto.getServiceFilePath() + fileName, nettySyncFileDto.getMinioPath());
                    }
                }
            }
        } else {
            logger.error("文件同步参数异常:{}", nettySyncFileDto);
        }
    }


}
