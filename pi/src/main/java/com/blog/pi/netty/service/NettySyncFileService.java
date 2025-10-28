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
     *
     * @param msgHead
     * @param nettySyncFileDto
     */
    public void receiveSyncFileMsg(MsgHead msgHead, NettySyncFileDto nettySyncFileDto) {
        logger.info("==== 服务器文件同步-netty接收服务器请求 ===== MsgHead: {} NettyFileSyncDto: {}", msgHead, nettySyncFileDto);

        sendReceiveMsg(msgHead);

        // 获取文件存储基础路径
        String basePath = Constant.BLOG_PATH_TEMP + "/" + MyStringUtils.getRandomString(6);

        SocketMoveFileDto moveFileDto = new SocketMoveFileDto();
        moveFileDto.setType(2);
        moveFileDto.setData(JSONObject.toJSONString(nettySyncFileDto));
        moveFileDto.setDirDeleteFlag(1);

        if (nettySyncFileDto.getSyncType() == 1) {
            // 下载服务器文件
            List<String> fileNameList = downloadFile(basePath, nettySyncFileDto, msgHead);

            moveFileDto.setSourceDirectory(basePath);
            moveFileDto.setTargetDirectory(nettySyncFileDto.getDeviceFilePath());
            moveFileDto.setFileNameList(fileNameList);
        } else if (nettySyncFileDto.getSyncType() == 2) {

            moveFileDto.setSourceDirectory(nettySyncFileDto.getDeviceFilePath());
            moveFileDto.setTargetDirectory(basePath);
            moveFileDto.setServicePath(nettySyncFileDto.getServiceFilePath());

            // 系统外文件不删除，执行备份操作
            if (nettySyncFileDto.getFileSource() == 2) {
                moveFileDto.setDirDeleteFlag(0);
            }

            // 文件存放在服务器挂载硬盘中，docker无法直接访问，先通知socket将文件移动到指定目录
            if (CollectionUtil.isNotEmpty(nettySyncFileDto.getFileNameList())) {
                // 指定了文件名称
                moveFileDto.setFileNameList(nettySyncFileDto.getFileNameList());
            } else {
                // 未指定文件名称
                moveFileDto.setCount(nettySyncFileDto.getCount());
            }
        }
        SocketPacket<SocketMoveFileDto> socketPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_MOVE_FILE, msgHead, moveFileDto);
        logger.info("===== socket 移动文件-服务器请求 ===== SocketPacket:{} NettySyncFileDto: {} basePath: {}", socketPacket, nettySyncFileDto, basePath);
        socketService.sendMessage("python", SocketConstant.LOCALHOST_REGISTER_CODE, socketPacket);
    }

    /**
     * 响应netty文件同步消息
     *
     * @param msgHead
     */
    private void sendReceiveMsg(MsgHead msgHead) {
        // 基础响应数据
        NettySyncFileDto baseResponse = new NettySyncFileDto();
        baseResponse.setSyncResult(1);
        baseResponse.setResultType(1);
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
    private void sendSocketMoveFileMsg(MsgHead msgHead, NettySyncFileDto nettySyncBlogFile, String basePath) {

    }

    /**
     * 上传文件
     *
     * @param responseMoveFileDto
     */
    public void receiveSocketMoveFileMsg(SocketMoveFileDto responseMoveFileDto, MsgHead msgHead) {
        logger.info("===== socket 移动文件-python脚本响应 ===== SocketMoveFileDto: {} MsgHead: {}", responseMoveFileDto, msgHead);
        NettySyncFileDto nettySyncFileDto = JSONObject.parseObject(responseMoveFileDto.getData(), NettySyncFileDto.class);
        if (nettySyncFileDto.getSyncType().equals(2)) {
            uploadFile(responseMoveFileDto, msgHead, nettySyncFileDto);
        }

        if (responseMoveFileDto.getDirDeleteFlag() == 1) {
            // 删除指定目录文件
        }
    }

    /**
     * 下载服务器文件
     *
     * @param basePath         本地存放基础路径
     * @param nettySyncFileDto netty 消息同步数据
     */
    private List<String> downloadFile(String basePath, NettySyncFileDto nettySyncFileDto, MsgHead msgHead) {
        String serviceFilePath = nettySyncFileDto.getServiceFilePath();
        List<String> successFileNameList = new ArrayList<>();
        String requestId = msgHead.getNettyMsgHead().getRequestId();

        for (String serviceFileName : nettySyncFileDto.getFileNameList()) {

            boolean syncResult = ftpUtil.downloadFtpFile(serviceFilePath, serviceFileName, basePath, serviceFileName);

            // 响应服务端处理结果
            NettySyncFileDto fileSyncDto = new NettySyncFileDto();
            fileSyncDto.setSyncType(1);
            fileSyncDto.setSyncResult(syncResult ? 1 : 0);
            fileSyncDto.setFileNameList(new ArrayList<>(List.of(serviceFilePath + "/" + serviceFileName)));
            NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(fileSyncDto));
            NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(msgHead, nettyResponse);
            nettyPacket.setMsgHead(msgHead);
            nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);

        }
        return successFileNameList;
    }

    /**
     * 上传文件至服务器
     *
     * @param responseMoveFileDto
     * @param msgHead
     * @param nettySyncFileDto
     */
    private void uploadFile(SocketMoveFileDto responseMoveFileDto, MsgHead msgHead, NettySyncFileDto nettySyncFileDto) {
        String serviceFilePath = nettySyncFileDto.getServiceFilePath();
        List<String> fileNameList = responseMoveFileDto.getFileNameList();
        String basePath = responseMoveFileDto.getTargetDirectory();
        for (int i = 0; i < fileNameList.size(); i++) {
            String fileName = fileNameList.get(i);

            if (nettySyncFileDto.getFileSource() == 1) {

            } else if (nettySyncFileDto.getFileSource() == 2) {
                // 上传系统外部文件时校验文件是否上传过
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
            }

            boolean uploadFlag = false;
            int retryTime = 3;
            for (int j = 0; j < retryTime && !(uploadFlag = ftpUtil.uploadFtpFile(basePath, fileName, serviceFilePath, fileName)); j++) {
                logger.warn("上传失败，第 {} 次重试中...", j + 1);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
            }
            if (uploadFlag) {
                logger.info("文件上传成功");
                boolean delFlag = deleteLocalFile(basePath, fileName);
                logger.info("文件删除结果: delFlag: {}",delFlag);
            } else {
                logger.error("文件上传失败，已重试 3 次");
                if (nettySyncFileDto.getFileSource() == 2) {
                    SocketMoveFileDto requestMoveFileDto = new SocketMoveFileDto();
                    requestMoveFileDto.setType(1);
                    requestMoveFileDto.setSourceDirectory(responseMoveFileDto.getTargetDirectory());
                    requestMoveFileDto.setTargetDirectory(Constant.DISK_PATH_TEMP + "/video");
                    requestMoveFileDto.setFileNameList(Collections.singletonList(fileName));
                    SocketPacket<SocketMoveFileDto> socketPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_MOVE_FILE, null, requestMoveFileDto);
                    socketPacket.setMsgHead(msgHead);
                    socketService.sendMessage("python", SocketConstant.LOCALHOST_REGISTER_CODE, socketPacket);
                }
            }

            // 上传完成一个文件
            String requestId = msgHead.getNettyMsgHead().getRequestId();

            // 响应服务端处理结果
            NettySyncFileDto fileSyncDto = new NettySyncFileDto();
            fileSyncDto.setResultType(2);
            fileSyncDto.setSyncType(2);
            fileSyncDto.setSyncResult(uploadFlag ? 1 : 0);
            if (i == fileNameList.size() - 1) {
                fileSyncDto.setSyncEnd(1);
            } else {
                fileSyncDto.setSyncEnd(0);
            }

            fileSyncDto.setServiceFilePath(nettySyncFileDto.getServiceFilePath());
            fileSyncDto.setFileNameList(new ArrayList<>(List.of(fileName)));
            fileSyncDto.setMinioPath(nettySyncFileDto.getMinioPath());

            NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(fileSyncDto));
            NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(msgHead, nettyResponse);
            nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);
        }
    }

    /**
     * 删除本地文件（简单版）
     *
     * @param dirPath  文件所在目录
     * @param fileName 文件名
     * @return 删除是否成功
     */
    public boolean deleteLocalFile(String dirPath, String fileName) {
        File file = new File(dirPath, fileName);
        return file.exists() && file.isFile() && file.delete();
    }
}
