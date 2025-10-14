package com.blog.pi.netty.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.pi.config.PiSystemConfig;
import com.blog.pi.domain.common.MsgHead;
import com.blog.pi.domain.entity.FileMD5;
import com.blog.pi.mapper.FileMD5Mapper;
import com.blog.pi.mapper.FileSyncMapper;
import com.blog.pi.ftp.FtpUtil;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.dto.NettyResponse;
import com.blog.pi.netty.dto.file.NettySyncFileDto;
import com.blog.pi.netty.dto.file.NettyFileSyncDto;
import com.blog.pi.netty.enums.NettyTopic;
import com.blog.pi.socket.SocketService;
import com.blog.pi.socket.domain.SocketPacket;
import com.blog.pi.socket.domain.constant.SocketConstant;
import com.blog.pi.socket.domain.constant.SocketTopic;
import com.blog.pi.socket.domain.dto.SocketMoveFileDto;
import com.blog.pi.utils.MD5Util;
import com.blog.pi.utils.MyStringUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

/**
 * @description: 同步博客文章服务类
 * @Author: lxk
 * @date 2024/1/11 11:20
 */

@Component
public class NettyFileSyncService {

    private static final Logger logger = LoggerFactory.getLogger(NettyFileSyncService.class);

    @Resource
    private FtpUtil ftpUtil;

    @Resource
    private NettyClient nettyClient;

    @Resource
    private SocketService socketService;

    @Resource
    private FileSyncMapper fileSyncDAO;

    @Resource
    private PiSystemConfig piSystemConfig;

    @Resource
    private FileMD5Mapper fileMD5Mapper;

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
    public void syncBlogFile(String data, String requestId, MsgHead msgHead) {
        logger.info("===== 文件同步 ===== data: {} requestId: {} MsgHead：{}", data, requestId, msgHead);

        // 基础响应数据
        NettyFileSyncDto baseResponse = new NettyFileSyncDto();
        baseResponse.setSyncResult(true);
        baseResponse.setSyncType(0);
        NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(baseResponse));
        NettyPacket<NettyResponse> basePacket = NettyPacket.buildResponse(requestId, NettyTopic.BLOG_FILE_SYNC, msgHead, nettyResponse);
        nettyClient.sendMsg(requestId, JSONObject.toJSONString(basePacket), false);

        // 解析netty接收数据
        NettySyncFileDto nettySyncBlogFile = JSON.parseObject(data, NettySyncFileDto.class);
        // 获取文件存储基础路径
        String basePath = "/opt/docker/files/temp" + "/" + MyStringUtils.getRandomString(6);

        if (nettySyncBlogFile.getSyncType().equals(1)) {
            // 下载服务器文件
            List<String> fileNameList = downloadFile(basePath, nettySyncBlogFile, msgHead, requestId);

            SocketMoveFileDto moveFileDto = new SocketMoveFileDto();
            moveFileDto.setRequestId(requestId);
            moveFileDto.setType(0);
            moveFileDto.setData(data);
            moveFileDto.setSourceDirectory(basePath);
            moveFileDto.setFileNameList(fileNameList);
            moveFileDto.setTargetDirectory("/opt/docker/files/temp/test" + "/" + MyStringUtils.getRandomString(6));
            SocketPacket<SocketMoveFileDto> socketPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_MOVE_FILE, msgHead, moveFileDto);
            socketPacket.setMsgHead(msgHead);
            socketService.sendMessage("python", SocketConstant.LOCALHOST_REGISTER_CODE, socketPacket);
        } else if (nettySyncBlogFile.getSyncType().equals(2)) {
            uploadBlogFileFirstStep(requestId, msgHead, nettySyncBlogFile, basePath);
        }
    }


    /**
     * 下载服务器文件
     *
     * @param basePath          本地存放基础路径
     * @param nettySyncBlogFile netty 消息同步数据
     */
    private List<String> downloadFile(String basePath, NettySyncFileDto nettySyncBlogFile, MsgHead msgHead, String requestId) {
        String serviceFilePath = nettySyncBlogFile.getServiceFilePath();
        List<String> successFileNameList = new ArrayList<>();

        for (String serviceFileName : nettySyncBlogFile.getFileNameList()) {

            Boolean syncResult = ftpUtil.downloadFtpFile(serviceFilePath, serviceFileName, basePath, serviceFileName);

            // 响应服务端处理结果
            NettyFileSyncDto fileSyncDto = new NettyFileSyncDto();
            fileSyncDto.setSyncType(1);
            fileSyncDto.setSyncResult(syncResult);
            fileSyncDto.setFileNameList(new ArrayList<>(List.of(serviceFilePath + "/" + serviceFileName)));
            NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(fileSyncDto));
            NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(requestId, NettyTopic.BLOG_FILE_SYNC, msgHead, nettyResponse);
            nettyPacket.setMsgHead(msgHead);
            nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);

        }
        return successFileNameList;
    }

    /**
     * 文件上传流程
     * 1. 服务器定时任务触发文件上传流程
     * 2. 树莓派设备接受数据并响应
     * 3. 通过websocket调用python脚本，将指定目录下文件移动到docker共享目录
     * 4. 等待websocket响应，调用updateBlogFileSecondStep
     *
     * @param requestId
     * @param msgHead
     * @param nettySyncBlogFile
     * @param basePath
     */
    private void uploadBlogFileFirstStep(String requestId, MsgHead msgHead, NettySyncFileDto nettySyncBlogFile, String basePath) {
        logger.info("===== socket 移动待上传文件-服务器请求 ===== requestId: {} NettySyncFileDto: {} basePath: {}", requestId, nettySyncBlogFile, basePath);
        SocketMoveFileDto moveFileDto = new SocketMoveFileDto();
        moveFileDto.setRequestId(requestId);
        moveFileDto.setType(0);
        moveFileDto.setData(JSONObject.toJSONString(nettySyncBlogFile));
        moveFileDto.setSourceDirectory(nettySyncBlogFile.getDeviceFilePath());
        moveFileDto.setTargetDirectory(basePath);
        moveFileDto.setServicePath(nettySyncBlogFile.getServiceFilePath());
        // 文件存放在服务器挂载硬盘中，docker无法直接访问，先通知socket将文件移动到指定目录
        if (CollectionUtil.isNotEmpty(nettySyncBlogFile.getFileNameList())) {
            // 指定了文件名称
            moveFileDto.setFileNameList(nettySyncBlogFile.getFileNameList());
        } else {
            // 未指定文件名称
            moveFileDto.setCount(nettySyncBlogFile.getCount());
        }
        SocketPacket<SocketMoveFileDto> message = SocketPacket.buildRequest(SocketTopic.SOCKET_MOVE_FILE, msgHead, moveFileDto);
        socketService.sendMessage("python", SocketConstant.LOCALHOST_REGISTER_CODE, message);
    }

    /**
     * 上传文件
     *
     * @param moveFileDto
     */
    public void updateBlogFileSecondStep(SocketMoveFileDto moveFileDto, MsgHead msgHead) {
        logger.info("===== socket 移动待上传文件-python脚本响应 ===== SocketMoveFileDto: {}", moveFileDto);
        NettySyncFileDto nettySyncFileDto = JSONObject.parseObject(moveFileDto.getData(), NettySyncFileDto.class);
        if (nettySyncFileDto.getSyncType().equals(2)) {
            List<String> fileNameList = moveFileDto.getFileNameList();
            for (String fileName : fileNameList) {
                // 上传文件
                logger.info("===== 当前上传文件 ===== fileName: {}", fileName);
                File file = new File(moveFileDto.getTargetDirectory() + "/" + fileName);
                String md5 = MD5Util.getFileMd5(file);
                LambdaQueryWrapper<FileMD5> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(FileMD5::getFileMD5, md5);
                FileMD5 fileMD5 = fileMD5Mapper.selectOne(wrapper);
                if (fileMD5 != null) {
                    logger.info("===== 文件重复 ===== fileName: {}", fileName);
                    fileMD5.setFileCount(fileMD5.getFileCount() + 1);
                    fileMD5Mapper.updateById(fileMD5);
                    continue;
                } else {
                    fileMD5 = new FileMD5();
                    fileMD5.setFileMD5(md5);
                    fileMD5.setFileCount(1);
                    fileMD5.setCreateTime(new Date());
                    fileMD5Mapper.insert(fileMD5);
                }

                Boolean uploadFlag = ftpUtil.uploadFtpFile(moveFileDto.getTargetDirectory(), fileName, moveFileDto.getServicePath(), fileName);

                // 上传完成一个文件
                String requestId = moveFileDto.getRequestId();

                // 响应服务端处理结果
                NettyFileSyncDto fileSyncDto = new NettyFileSyncDto();
                fileSyncDto.setServiceFilePath(moveFileDto.getServicePath());
                fileSyncDto.setSyncType(2);
                fileSyncDto.setSyncResult(uploadFlag);
                fileSyncDto.setFileNameList(new ArrayList<>(List.of(fileName)));
                fileSyncDto.setMinioPath(nettySyncFileDto.getMinioPath());
                NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(fileSyncDto));
                NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(requestId, NettyTopic.BLOG_FILE_SYNC, msgHead, nettyResponse);
                nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);
            }
        }
    }
}
