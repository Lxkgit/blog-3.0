package com.blog.file.netty.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.file.device.entity.UserDevice;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.minio.MinioService;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.domain.enums.NettyTopic;
import com.blog.file.socket.SocketService;
import com.blog.file.socket.domain.SocketPacket;
import com.blog.file.socket.domain.constant.SocketClientType;
import com.blog.file.socket.domain.constant.SocketConstant;
import com.blog.file.socket.domain.constant.SocketTopic;
import com.blog.file.socket.domain.dto.SocketExportBlogFileDto;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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
    private SocketService socketService;

    @Resource
    private UserDeviceMapper userDeviceMapper;

    /**
     * netty消息发送文件同步到服务器
     * @param nettySyncFileDto 同步文件参数
     */
    public void syncFileSend(NettySyncFileDto nettySyncFileDto) {
//        Integer userId = SecurityUtil.getLoginUser().getId();

        // 获取用户默认同步数据设备
        LambdaQueryWrapper<UserDevice> userDeviceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userDeviceLambdaQueryWrapper.eq(UserDevice::getUserId, 1);
        List<UserDevice> deviceList = userDeviceMapper.selectList(userDeviceLambdaQueryWrapper);

        if (CollectionUtils.isNotEmpty(deviceList)) {
            UserDevice device = deviceList.get(0);
            String registerId = device.getDeviceCode();

            NettyPacket<NettySyncFileDto> nettyPacket = NettyPacket.buildRequest(NettyTopic.BLOG_FILE_SYNC, nettySyncFileDto);
            nettyServer.channelWriteByRegisterId(registerId, JSON.toJSONString(nettyPacket), true);
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

    /**
     * 博客数据同步任务-第一步
     * 发送socket导出博客数据任务
     */
    public void syncBlogDataFirstStep() {
        SocketExportBlogFileDto exportBlogFileDto = new SocketExportBlogFileDto();
        exportBlogFileDto.setBlogFilePath(Constant.FTP_PATH_SYSTEM + "/temp/" + MyStringUtils.getRandomString(6));
        SocketPacket<SocketExportBlogFileDto> requestPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_EXPORT_BLOG_FILE, exportBlogFileDto);
        socketService.sendMessage(SocketClientType.PYTHON, SocketConstant.LOCALHOST_REGISTER_CODE, requestPacket);
    }

    /**
     * 博客数据同步任务-第二步
     * 收到socket消息，组合netty消息，发送到设备
     */
    public void syncBlogDataSecondStep(String data) {

        SocketExportBlogFileDto socketExportBlogFileDto = JSONObject.parseObject(data, SocketExportBlogFileDto.class);
        // 此处将文件在ftp的全路径转换为在ftp/system用户目录下的路径
        String serviceFilePath = socketExportBlogFileDto.getBlogFilePath().substring(Constant.FTP_PATH_SYSTEM.length());
        String fileName = socketExportBlogFileDto.getBlogFileName();
        String deviceFilePath = "/opt/docker/files/temp";

        NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToDevice(serviceFilePath, deviceFilePath);
        nettySyncFileDto.setFileNameList(List.of(fileName));
        syncFileSend(nettySyncFileDto);
    }

    public void syncDeviceFile() {
        // 文件存储minio中路径
        String minioPath = "/1/device";
        // servicePath 为文件在ftp system用户目录下的相对路径
        String servicePath = "/temp/" + MyStringUtils.getRandomString(6);
        // devicePath 为树莓派设备上的绝对路径
        String devicePath = "/mnt/test";
        NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToService(minioPath, servicePath, devicePath);
        nettySyncFileDto.setCount(3);
        syncFileSend(nettySyncFileDto);
    }


}
