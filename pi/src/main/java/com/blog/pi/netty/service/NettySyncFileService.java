package com.blog.pi.netty.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.pi.constant.Constant;
import com.blog.pi.domain.common.MsgHead;
import com.blog.pi.domain.entity.FileMD5;
import com.blog.pi.mapper.FileMD5Mapper;
import com.blog.pi.ftp.FtpUtil;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.dto.NettyResponse;
import com.blog.pi.netty.dto.file.NettySyncFileDto;
import com.blog.pi.socket.SocketService;
import com.blog.pi.socket.domain.SocketPacket;
import com.blog.pi.socket.domain.constant.SocketConstant;
import com.blog.pi.socket.domain.constant.SocketTopic;
import com.blog.pi.socket.domain.dto.SocketMoveFileDto;
import com.blog.pi.utils.MD5Util;
import com.blog.pi.utils.MyStringUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
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
public class NettySyncFileService {

    private static final Logger logger = LoggerFactory.getLogger(NettySyncFileService.class);

    @Resource
    private FtpUtil ftpUtil;

    @Resource
    private NettyClient nettyClient;

    @Resource
    private SocketService socketService;

    @Resource
    private FileMD5Mapper fileMD5Mapper;


    /**
     * netty 文件同步消息流程
     * 1. 接收到netty请求，对该请求相应
     * 2. ftp下载或上传指定文件
     * 3. 发送ftp文件上传完成的请求信息
     * 5. 服务器响应文件上传结果
     * @param msgHead
     * @param nettySyncFileDto
     */
    public void receiveSyncFileMsg(MsgHead msgHead, NettySyncFileDto nettySyncFileDto) {
        logger.info("==== 服务器文件同步-netty接收服务器请求 ===== MsgHead: {} NettyFileSyncDto: {}", msgHead, nettySyncFileDto);

        sendReceiveMsg(msgHead);

        // 获取文件存储基础路径
        String basePath = Constant.BLOG_PATH_TEMP + "/" + MyStringUtils.getRandomString(6);

        if (nettySyncFileDto.getSyncType() == 1) {
            // 下载服务器文件
            List<String> fileNameList = downloadFile(basePath, nettySyncFileDto, msgHead);

            SocketMoveFileDto moveFileDto = new SocketMoveFileDto();
            moveFileDto.setType(1);
            moveFileDto.setData(JSONObject.toJSONString(nettySyncFileDto));
            moveFileDto.setSourceDirectory(basePath);
            moveFileDto.setTargetDirectory(nettySyncFileDto.getDeviceFilePath());
            moveFileDto.setFileNameList(fileNameList);
            moveFileDto.setDirDeleteFlag(1);
            SocketPacket<SocketMoveFileDto> socketPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_MOVE_FILE, msgHead, moveFileDto);
            socketPacket.setMsgHead(msgHead);
            socketService.sendMessage("python", SocketConstant.LOCALHOST_REGISTER_CODE, socketPacket);
        } else if (nettySyncFileDto.getSyncType() == 2) {
            // 文件上传至服务器
            uploadBlogFileFirstStep(msgHead, nettySyncFileDto, basePath);
        }
    }

    /**
     * 响应netty文件同步消息
     *
     * @param msgHead
     */
    private void sendReceiveMsg(MsgHead msgHead) {
        // 基础响应数据
        NettySyncFileDto baseResponse = new NettySyncFileDto();
        baseResponse.setSyncResult(true);
        baseResponse.setSyncType(0);
        NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(baseResponse));
        NettyPacket<NettyResponse> basePacket = NettyPacket.buildResponse(msgHead, nettyResponse);
        nettyClient.sendMsg(msgHead.getNettyMsgHead().getRequestId(), JSONObject.toJSONString(basePacket), false);
    }




//    /**
//     * 下载服务器指定文件
//     * 业务流程：
//     * 1. 接收到netty请求，对该请求相应
//     * 2. ftp下载或上传指定文件
//     * 3. 发送ftp文件上传完成的请求信息
//     * 4. 服务器校验文件信息
//     * 5. 服务器响应文件上传结果
//     */
//    public void syncBlogFile(NettySyncFileDto nettySyncBlogFile, MsgHead msgHead) {
//        logger.info("===== 文件同步 ===== NettySyncFileDto: {} MsgHead：{}", nettySyncBlogFile, msgHead);
//
//        String requestId = msgHead.getNettyMsgHead().getRequestId();
//
//        sendReceiveMsg(msgHead, requestId);
//
//        // 获取文件存储基础路径
//        String basePath = "/opt/docker/files/temp/blogBak/";
//
//        if (nettySyncBlogFile.getSyncType().equals(1)) {
//            // 下载服务器文件
//            List<String> fileNameList = downloadFile(basePath, nettySyncBlogFile, msgHead);
//
//            SocketMoveFileDto moveFileDto = new SocketMoveFileDto();
//            moveFileDto.setType(1);
//            moveFileDto.setData(nettySyncBlogFile.toString());
//            moveFileDto.setSourceDirectory(basePath);
//            moveFileDto.setFileNameList(fileNameList);
//            moveFileDto.setTargetDirectory(Constant.DISK_PATH_TEMP + "/blogBak");
//            SocketPacket<SocketMoveFileDto> socketPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_MOVE_FILE, msgHead, moveFileDto);
//            socketPacket.setMsgHead(msgHead);
//            socketService.sendMessage("python", SocketConstant.LOCALHOST_REGISTER_CODE, socketPacket);
//        } else if (nettySyncBlogFile.getSyncType().equals(2)) {
//            uploadBlogFileFirstStep(msgHead, nettySyncBlogFile, basePath);
//        }
//    }


    /**
     *
     * @param basePath          本地存放基础路径
     * @param nettySyncBlogFile netty 消息同步数据
     */
    private List<String> downloadFile(String basePath, NettySyncFileDto nettySyncBlogFile, MsgHead msgHead) {
        String serviceFilePath = nettySyncBlogFile.getServiceFilePath();
        List<String> successFileNameList = new ArrayList<>();
        String requestId = msgHead.getNettyMsgHead().getRequestId();

        for (String serviceFileName : nettySyncBlogFile.getFileNameList()) {

            Boolean syncResult = ftpUtil.downloadFtpFile(serviceFilePath, serviceFileName, basePath, serviceFileName);

            // 响应服务端处理结果
            NettySyncFileDto fileSyncDto = new NettySyncFileDto();
            fileSyncDto.setSyncType(1);
            fileSyncDto.setSyncResult(syncResult);
            fileSyncDto.setFileNameList(new ArrayList<>(List.of(serviceFilePath + "/" + serviceFileName)));
            NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(fileSyncDto));
            NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(msgHead, nettyResponse);
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
     * @param msgHead
     * @param nettySyncBlogFile
     * @param basePath
     */
    private void uploadBlogFileFirstStep(MsgHead msgHead, NettySyncFileDto nettySyncBlogFile, String basePath) {
        logger.info("===== socket 移动待上传文件-服务器请求 ===== NettySyncFileDto: {} basePath: {}", nettySyncBlogFile, basePath);
        SocketMoveFileDto moveFileDto = new SocketMoveFileDto();
        moveFileDto.setType(2);
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
     * @param responseMoveFileDto
     */
    public void updateBlogFileSecondStep(SocketMoveFileDto responseMoveFileDto, MsgHead msgHead) {
        logger.info("===== socket 移动待上传文件-python脚本响应 ===== SocketMoveFileDto: {} MsgHead: {}", responseMoveFileDto, msgHead);
        NettySyncFileDto nettySyncFileDto = JSONObject.parseObject(responseMoveFileDto.getData(), NettySyncFileDto.class);
        if (nettySyncFileDto.getSyncType().equals(2)) {
            List<String> fileNameList = responseMoveFileDto.getFileNameList();
            for (String fileName : fileNameList) {
                // 上传文件
                logger.info("===== 当前上传文件 ===== fileName: {}", fileName);
                File file = new File(responseMoveFileDto.getTargetDirectory() + "/" + fileName);
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

                Boolean uploadFlag = ftpUtil.uploadFtpFile(responseMoveFileDto.getTargetDirectory(), fileName, responseMoveFileDto.getServicePath(), fileName);

                // 上传完成一个文件
                String requestId = msgHead.getNettyMsgHead().getRequestId();

                // 响应服务端处理结果
                NettySyncFileDto fileSyncDto = new NettySyncFileDto();
                fileSyncDto.setServiceFilePath(responseMoveFileDto.getServicePath());
                fileSyncDto.setSyncType(2);
                fileSyncDto.setSyncResult(uploadFlag);
                fileSyncDto.setFileNameList(new ArrayList<>(List.of(fileName)));
                fileSyncDto.setMinioPath(nettySyncFileDto.getMinioPath());
                NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(fileSyncDto));
                NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(msgHead, nettyResponse);
                nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);
            }
        }

        SocketMoveFileDto requestMoveFileDto = new SocketMoveFileDto();
        requestMoveFileDto.setType(1);
        requestMoveFileDto.setSourceDirectory(responseMoveFileDto.getTargetDirectory());
        requestMoveFileDto.setTargetDirectory(Constant.DISK_PATH_TEMP + "/video");
        SocketPacket<SocketMoveFileDto> socketPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_MOVE_FILE, null, requestMoveFileDto);
        socketPacket.setMsgHead(msgHead);
        socketService.sendMessage("python", SocketConstant.LOCALHOST_REGISTER_CODE, socketPacket);

    }
}
