package com.blog.pi.netty.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.pi.config.PiSystemConfig;
import com.blog.pi.dao.FileSyncDAO;
import com.blog.pi.domain.entity.FileSync;
import com.blog.pi.ftp.FtpUtil;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.dto.NettyResponse;
import com.blog.pi.netty.dto.file.NettySyncFileDto;
import com.blog.pi.netty.enums.NettyTopicEnum;
import com.blog.pi.socket.SocketService;
import com.blog.pi.socket.domain.SocketPacket;
import com.blog.pi.socket.domain.constant.SocketConstant;
import com.blog.pi.socket.domain.constant.SocketTopic;
import com.blog.pi.socket.domain.dto.SocketMoveFileDto;
import com.blog.pi.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;

/**
 * @description: 同步博客文章服务类
 * @Author: lxk
 * @date 2024/1/11 11:20
 */

@Component
public class NettyFileSyncService {

    @Resource
    private FtpUtil ftpUtil;

    @Resource
    private NettyClient nettyClient;

    @Resource
    private SocketService socketService;

    @Resource
    private FileSyncDAO fileSyncDAO;

    @Resource
    private PiSystemConfig piSystemConfig;

    /**
     * 下载服务器指定文件
     * 业务流程：
     * 1. 接收到netty请求，对该请求相应
     * 2. ftp下载或上传指定文件
     * 3. 发送ftp文件上传完成的请求信息
     * 4. 服务器校验文件信息
     * 5. 服务器响应文件上传结果
     *
     * @param data      netty收到的消息
     * @param requestId 本次请求唯一编码
     */
    public void syncBlogFile(String data, String requestId) {
        // 响应服务端处理结果
        NettyResponse nettyResponse = new NettyResponse(true);
        NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(requestId, NettyTopicEnum.BLOG_FILE_SYNC.getTopic(), nettyResponse);
        nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);

        // 解析netty接收数据
        NettySyncFileDto nettySyncBlogFile = JSON.parseObject(data, NettySyncFileDto.class);
        // 获取文件存储基础路径
        String basePath = (String) piSystemConfig.getRegisterConfig("ftp", "basePath");

        if (nettySyncBlogFile.getSyncType().equals(0)) {
            // 下载服务器文件
            downloadFile(basePath, nettySyncBlogFile);
        } else if (nettySyncBlogFile.getSyncType().equals(1)) {
            // 上传文件至服务器
            uploadFile(nettySyncBlogFile);
        } else if (nettySyncBlogFile.getSyncType().equals(2)) {
            downloadBlogData(basePath, nettySyncBlogFile);
        }
    }

    /**
     * 博客数据文件备份
     *
     * @param basePath
     * @param nettySyncBlogFile
     */
    private void downloadBlogData(String basePath, NettySyncFileDto nettySyncBlogFile) {
        String serviceFilePath = nettySyncBlogFile.getServiceFilePath();
        List<String> fileNameList = nettySyncBlogFile.getFileNameList();

        for (String fileName : fileNameList) {
            String appendPath = "/sync";
            String localFileName = "blog.zip";
            boolean success = ftpUtil.downloadFtpFile(serviceFilePath, fileName, basePath + appendPath, localFileName);

            if (success) {
                File file = new File(basePath + appendPath + File.separatorChar + localFileName);
                FileSync fileSync = new FileSync();
                fileSync.setUserId(nettySyncBlogFile.getUserId());
                fileSync.setFileCode(nettySyncBlogFile.getFileCode());
                fileSync.setServiceFilePath(serviceFilePath);
                fileSync.setServiceFileName(fileName);
                fileSync.setLocalFilePath(basePath + appendPath);
                fileSync.setLocalFileName(localFileName);
                fileSync.setFileSize(file.length());
                fileSync.setCreateTime(new Date());
                fileSyncDAO.insert(fileSync);
            }
        }

    }

    /**
     * 上传文件至服务器
     */
    private void uploadFile(NettySyncFileDto nettySyncBlogFile) {
        QueryWrapper<FileSync> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_code", nettySyncBlogFile.getFileCode());
        queryWrapper.eq("user_id", nettySyncBlogFile.getUserId());
        List<FileSync> fileSyncList = fileSyncDAO.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(fileSyncList)) {
            FileSync fileSync = fileSyncList.get(0);
            boolean success = ftpUtil.uploadFtpFile(fileSync.getLocalFilePath(), fileSync.getLocalFileName(), fileSync.getServiceFilePath(), fileSync.getServiceFileName());

            if (success) {
                fileSyncDAO.deleteById(fileSync);
            }
        }
    }

    /**
     * 下载服务器文件
     *
     * @param basePath          本地存放基础路径
     * @param nettySyncBlogFile netty 消息同步数据
     */
    private void downloadFile(String basePath, NettySyncFileDto nettySyncBlogFile) {
        String serviceFilePath = nettySyncBlogFile.getServiceFilePath();
        List<String> fileNameList = nettySyncBlogFile.getFileNameList();

        for (String serviceFileName : fileNameList) {
            String fileType = serviceFileName.substring(serviceFileName.lastIndexOf("."));
            String fileName = serviceFileName.substring(0, serviceFileName.lastIndexOf("."));

            String filePath = basePath + serviceFilePath;

            String localFileName = fileName + "_" + StringUtils.getRandomString(6) + fileType;
            boolean success = ftpUtil.downloadFtpFile(serviceFilePath, serviceFileName, filePath, localFileName);

            if (success) {
                File file = new File(basePath + File.separatorChar + localFileName);
                FileSync fileSync = new FileSync();
                fileSync.setUserId(nettySyncBlogFile.getUserId());
                fileSync.setFileCode(nettySyncBlogFile.getFileCode());
                fileSync.setServiceFilePath(serviceFilePath);
                fileSync.setServiceFileName(serviceFileName);
                fileSync.setLocalFilePath(filePath);
                fileSync.setLocalFileName(localFileName);
                fileSync.setFileSize(file.length());
                fileSync.setCreateTime(new Date());
                fileSyncDAO.insert(fileSync);
            }
        }
    }

    /**
     * 文件上传流程
     * 1. 服务器定时任务触发文件上传流程
     * 2. 树莓派设备接受数据并响应
     * 3. 通过websocket调用python脚本，将指定目录下文件移动到docker共享目录
     * 4. 等待websocket响应，调用updateBlogFileSecondStep
     *
     * @param data
     * @param requestId
     */
    public void uploadBlogFileFirstStep(String data, String requestId) {
        // 响应服务端处理结果
        Map<String, Object> map = new HashMap<>();
        map.put("resultType", 1);
        NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(map));
        NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(requestId, NettyTopicEnum.BLOG_FILE_UPLOAD.getTopic(), nettyResponse);
        nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);

        // 解析netty接收数据
        NettySyncFileDto nettySyncFileDto = JSON.parseObject(data, NettySyncFileDto.class);

        SocketPacket<SocketMoveFileDto> message = new SocketPacket<>();
        message.setTopic(SocketTopic.SOCKET_MOVE_FILE);
        SocketMoveFileDto moveFileDto = new SocketMoveFileDto();
        moveFileDto.setRequestId(requestId);
        moveFileDto.setSourceDirectory(nettySyncFileDto.getDeviceFilePath());
        moveFileDto.setTargetDirectory(nettySyncFileDto.getServiceFilePath());
        moveFileDto.setCount(10);
        message.setData(moveFileDto);
        socketService.sendMessage("python", SocketConstant.LOCALHOST_REGISTER_CODE, message);
    }


    /**
     * 上传文件
     *
     * @param moveFileDto
     * @param fileNameList
     */
    public void updateBlogFileSecondStep(SocketMoveFileDto moveFileDto, List<String> fileNameList) {
        for (String fileName : fileNameList) {
            ftpUtil.uploadFtpFile(moveFileDto.getTargetDirectory(), fileName, moveFileDto.getServicePath(), fileName);
        }

        // 上传完成之后再次响应数据
        String requestId = moveFileDto.getRequestId();
        // 响应服务端处理结果
        Map<String, Object> map = new HashMap<>();
        map.put("resultType", 2);
        map.put("filePath", moveFileDto.getServicePath());
        map.put("fileNameList", fileNameList);
        NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(map));
        NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(requestId, NettyTopicEnum.BLOG_FILE_UPLOAD.getTopic(), nettyResponse);
        nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);
    }
}
