package com.blog.pi.netty.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.netty.dto.NettyPacket;
import com.blog.core.domain.netty.dto.NettyResponse;
import com.blog.core.domain.netty.dto.file.NettySyncFileDto;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.socket.SocketPacket;
import com.blog.core.domain.socket.constant.SocketConstant;
import com.blog.core.domain.socket.constant.SocketTopic;
import com.blog.core.domain.socket.dto.SocketMoveFileDto;
import com.blog.pi.constant.Constant;
import com.blog.pi.domain.entity.FileMD5;
import com.blog.pi.mapper.FileMD5Mapper;
import com.blog.pi.ftp.FtpUtil;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.socket.SocketService;
import com.blog.pi.utils.MD5Util;
import com.blog.pi.utils.MyStringUtils;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
            moveFileDto.setFileSource(nettySyncFileDto.getFileSource());

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
     * 接收socket移动文件响应
     *
     * @param responseMoveFileDto
     */
    public void receiveSocketMoveFileMsg(SocketMoveFileDto responseMoveFileDto, MsgHead msgHead) {
        logger.info("===== socket 移动文件-python脚本响应 ===== SocketMoveFileDto: {} MsgHead: {}", responseMoveFileDto, msgHead);
        NettySyncFileDto nettySyncFileDto = JSONObject.parseObject(responseMoveFileDto.getData(), NettySyncFileDto.class);
        if (nettySyncFileDto.getSyncType().equals(1)) {
            responseNettyMsg(nettySyncFileDto, msgHead, true, responseMoveFileDto.getFileNameList());
        } else if (nettySyncFileDto.getSyncType().equals(2)) {
            uploadFile(responseMoveFileDto, msgHead, nettySyncFileDto);
        }

        if (responseMoveFileDto.getDirDeleteFlag() != null && responseMoveFileDto.getDirDeleteFlag() == 1) {
            // 删除指定目录文件
            if (nettySyncFileDto.getSyncEnd() == 1) {

            }
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

        List<String> fileNameList = nettySyncFileDto.getFileNameList();
        for (int i = 0; i < fileNameList.size(); i++) {

            String serviceFileName = fileNameList.get(i);
            boolean syncResult = false;
            try {
                syncResult = ftpUtil.downloadFtpFile(serviceFilePath, serviceFileName, basePath, serviceFileName);
            } catch (Exception e) {
                logger.info("文件下载异常: {}", e.getMessage(), e);
                nettySyncFileDto.setErrorMsg(e.getMessage());
            }

            if (syncResult) {
                successFileNameList.add(serviceFileName);
            }

            logger.info("文件下载完成: {}", fileNameList);
            responseNettyMsg(nettySyncFileDto, msgHead, syncResult, fileNameList, i);
        }
        return successFileNameList;
    }

    /**
     * 上传文件至服务器
     *
     * @param responseMoveFileDto
     * @param msgHead
     * @param nettySyncParam
     */
    private void uploadFile(SocketMoveFileDto responseMoveFileDto, MsgHead msgHead, NettySyncFileDto nettySyncParam) {
        String serviceFilePath = nettySyncParam.getServiceFilePath();
        List<String> fileNameList = responseMoveFileDto.getFileNameList();
        String basePath = responseMoveFileDto.getTargetDirectory();
        for (int i = 0; i < fileNameList.size(); i++) {
            NettySyncFileDto nettySyncFileDto = new NettySyncFileDto();
            BeanUtils.copyProperties(nettySyncParam, nettySyncFileDto);
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
                    nettySyncFileDto.setErrorMsg("文件重复 fileName: " + fileName);
                    responseNettyMsg(nettySyncFileDto, msgHead, false, fileNameList, i);
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
            try {
                uploadFlag = ftpUtil.uploadFtpFile(basePath, fileName, serviceFilePath, fileName);
            } catch (Exception e) {
                logger.info("文件上传异常: {}", e.getMessage(), e);
                nettySyncFileDto.setErrorMsg(e.getMessage());
            }

            uploadFileResult(responseMoveFileDto, msgHead, nettySyncFileDto, uploadFlag, basePath, fileName);

            responseNettyMsg(nettySyncFileDto, msgHead, uploadFlag, fileNameList, i);
        }
    }

    private void uploadFileResult(SocketMoveFileDto responseMoveFileDto, MsgHead msgHead, NettySyncFileDto nettySyncFileDto, boolean uploadFlag, String basePath, String fileName) {
        if (uploadFlag) {
            logger.info("文件上传成功");
            boolean delFlag = deleteLocalFile(basePath, fileName);
            logger.info("文件删除结果: delFlag: {}", delFlag);
        } else {
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
    }

    /**
     * 响应netty文件同步消息
     *
     * @param msgHead netty 消息头
     */
    private void sendReceiveMsg(MsgHead msgHead) {
        // 基础响应数据
        NettySyncFileDto baseResponse = new NettySyncFileDto();
        baseResponse.setResultType(1);
        NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(baseResponse));
        NettyPacket<NettyResponse> basePacket = NettyPacket.buildResponse(msgHead, nettyResponse);
        nettyClient.sendMsg(msgHead.getNettyMsgHead().getRequestId(), JSONObject.toJSONString(basePacket), false);
    }

    /**
     * 文件上传、下载成功后响应netty消息
     *
     * @param nettySyncFileDto Netty 同步文件类
     * @param msgHead          Netty 消息头
     * @param syncResult       文件同步结果
     * @param fileNameList     文件同步列表
     */
    private void responseNettyMsg(NettySyncFileDto nettySyncFileDto, MsgHead msgHead, boolean syncResult, List<String> fileNameList) {
        responseNettyMsg(nettySyncFileDto, msgHead, syncResult, fileNameList, null);
    }

    /**
     * 文件上传、下载成功后响应netty消息
     *
     * @param nettySyncFileDto netty同步文件类
     * @param msgHead          netty 消息头
     * @param syncResult       文件同步结果
     * @param fileNameList     文件同步列表
     * @param i                响应文件顺序
     */
    private void responseNettyMsg(NettySyncFileDto nettySyncFileDto, MsgHead msgHead, boolean syncResult, List<String> fileNameList, Integer i) {
        // 响应服务端处理结果
        NettySyncFileDto fileSyncDto = new NettySyncFileDto();

        if (i != null) {
            fileSyncDto.setFileNameList(List.of(fileNameList.get(i)));
            fileSyncDto.setMinioDeleteFlag(nettySyncFileDto.getMinioDeleteFlag());
            // 下载指定文件时响应永远为未结束，上传按照列表判断是否接收
            if (nettySyncFileDto.getSyncType() == 1) {
                fileSyncDto.setSyncEnd(0);
            } else if (nettySyncFileDto.getSyncType() == 2) {
                if (nettySyncFileDto.getFileSource() == 1) {
                    // 系统内文件上传时需要携带文件编码
                    fileSyncDto.setFileCodeList(List.of(nettySyncFileDto.getFileCodeList().get(i)));
                }
                // 上传文件为最后一个文件时返回上传任务结束
                fileSyncDto.setSyncEnd(i == fileNameList.size() - 1 ? 1 : 0);
            }
        } else {
            // 下载文件接收到socket移动文件响应后结束下载
            fileSyncDto.setFileNameList(fileNameList);
            fileSyncDto.setSyncEnd(1);
        }

        String requestId = msgHead.getNettyMsgHead().getRequestId();

        fileSyncDto.setResultType(2);
        fileSyncDto.setSyncType(nettySyncFileDto.getSyncType());
        fileSyncDto.setSyncResult(syncResult ? 1 : 0);
        fileSyncDto.setSyncCount(nettySyncFileDto.getSyncCount());

        fileSyncDto.setServiceFilePath(nettySyncFileDto.getServiceFilePath());
        fileSyncDto.setDeviceFilePath(nettySyncFileDto.getDeviceFilePath());
        fileSyncDto.setMinioPath(nettySyncFileDto.getMinioPath());
        fileSyncDto.setFileSource(nettySyncFileDto.getFileSource());

        fileSyncDto.setErrorMsg(nettySyncFileDto.getErrorMsg());

        NettyResponse nettyResponse = new NettyResponse(true, JSONObject.toJSONString(fileSyncDto));
        NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(msgHead, nettyResponse);
        nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);
    }

    /**
     * 删除本地文件
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
