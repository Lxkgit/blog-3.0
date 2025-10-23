package com.blog.file.netty.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.file.device.entity.UserDevice;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.task.bo.SyncDeviceFileBo;
import com.blog.core.utils.MyStringUtils;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.domain.enums.NettyTopic;
import com.blog.file.service.FileService;
import com.blog.file.socket.domain.SocketPacket;
import com.blog.file.socket.domain.constant.SocketClientType;
import com.blog.file.socket.domain.constant.SocketConstant;
import com.blog.file.socket.domain.constant.SocketTopic;
import com.blog.file.socket.domain.dto.SocketExportBlogFileDto;
import com.blog.file.socket.domain.service.SocketMessageSendService;
import com.blog.file.socket.service.SocketService;
import com.blog.file.task.TaskLogService;
import com.blog.redis.service.RedisService;
import com.blog.task.constant.TaskConstant;
import com.blog.task.domain.TaskBase;
import com.blog.task.domain.TaskEntity;
import com.blog.task.service.CreateTaskService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @description: Netty文件同步业务
 * @Author: lxk
 * @date 2024/3/11 11:41
 */

@Service
public class NettySyncFileService {

    private static final Logger logger = LoggerFactory.getLogger(NettySyncFileService.class);

    @Resource
    private FileService fileService;

    @Resource
    private NettyServer nettyServer;

    @Resource
    private SocketService socketService;

    @Resource
    private CreateTaskService createTaskService;

    @Resource
    private UserDeviceMapper userDeviceMapper;

    @Resource
    private SocketMessageSendService socketMessageSendService;

    @Resource
    private RedisService redisService;

    @Resource
    private TaskLogService taskLogService;

    @Resource
    private FileCategoryMapper fileCategoryMapper;

    @Resource
    private FileCategoryDataMapper fileCategoryDataMapper;

    /**
     * 发送文件同步消息至树莓派
     *
     * @param nettySyncFileDto 同步文件参数
     */
    public void sendSyncFileMsg(MsgHead msgHead, NettySyncFileDto nettySyncFileDto, Integer userId) {

        // 获取用户默认同步数据设备
        LambdaQueryWrapper<UserDevice> userDeviceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userDeviceLambdaQueryWrapper.eq(UserDevice::getUserId, userId);
        List<UserDevice> deviceList = userDeviceMapper.selectList(userDeviceLambdaQueryWrapper);

        if (CollectionUtils.isNotEmpty(deviceList)) {
            UserDevice device = deviceList.get(0);
            String registerId = device.getDeviceCode();

            NettyPacket<NettySyncFileDto> nettyPacket = NettyPacket.buildRequest(NettyTopic.BLOG_FILE_SYNC, nettySyncFileDto);
            nettyPacket.setMsgHead(msgHead);
            nettyServer.sendByRegisterIdLimitTime(registerId, JSON.toJSONString(nettyPacket), 8 * 60);
        }
    }

    /**
     * 接收文件同步消息
     *
     * @param msgHead
     * @param nettySyncFileDto
     */
    public void receiveSyncFileMsg(MsgHead msgHead, NettySyncFileDto nettySyncFileDto) {
        logger.info("==== 服务器文件同步-netty消息响应处理 ===== MsgHead: {} NettyFileSyncDto: {}", msgHead, nettySyncFileDto);
        if (nettySyncFileDto.getResultType() == 1) {
            logger.info("收到netty响应消息");
        } else if (nettySyncFileDto.getResultType() == 2) {
            if (nettySyncFileDto.getSyncResult()) {
                if (nettySyncFileDto.getSyncType() == 1) {
                    // 文件下载消息响应

                    fileService.fileDownloadDevice(nettySyncFileDto, msgHead);
                } else if (nettySyncFileDto.getSyncType() == 2) {
                    // 文件上传消息响应

                    // 文件导入minio
                    fileService.fileImportMinio(nettySyncFileDto, msgHead);
                }
                // 文件下载或上传成功之后删除临时目录
                deleteTempFile(nettySyncFileDto.getServiceFilePath(), "1m");
            } else {
                logger.info("文件同步失败");
            }
        }

        // 文件同步 任务请求头不为空时记录任务日志
        if (msgHead != null && msgHead.getTaskMsgHead() != null && StringUtils.isNotEmpty(msgHead.getTaskMsgHead().getTaskUUID())) {
            taskLogService.recordTaskLog(nettySyncFileDto, msgHead);
        }
    }

    /**
     * 博客数据同步任务-第一步
     * 发送socket导出博客数据任务
     */
    public String syncBlogDataFirstStep(MsgHead msgHead) {
        logger.info("===== 定时任务-博客数据同步-socket导出数据 ===== MsgHead: {}", msgHead);
        SocketExportBlogFileDto exportBlogFileDto = new SocketExportBlogFileDto();
        String blogFilePath = Constant.FTP_PATH_SYSTEM + "/temp/" + MyStringUtils.getRandomString(6);
        exportBlogFileDto.setBlogFilePath(blogFilePath);

        SocketPacket<SocketExportBlogFileDto> requestPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_EXPORT_BLOG_FILE,
                msgHead, exportBlogFileDto);
        socketService.sendMessage(SocketClientType.PYTHON, SocketConstant.LOCALHOST_REGISTER_CODE, requestPacket);
        deleteTempFile(blogFilePath, "25h");
        return blogFilePath;
    }

    /**
     * 博客数据同步任务-第二步
     * 收到socket消息，组合netty消息，发送到设备
     */
    public void syncBlogDataSecondStep(String data, MsgHead msgHead) {
        logger.info("===== 定时任务-博客数据同步-文件同步树莓派 ===== data：{} MsgHead: {}", data, msgHead);
        SocketExportBlogFileDto socketExportBlogFileDto = JSONObject.parseObject(data, SocketExportBlogFileDto.class);

        // 此处将文件在ftp的全路径转换为在ftp/system用户目录下的路径
        String serviceFilePath = socketExportBlogFileDto.getBlogFilePath().substring(Constant.FTP_PATH_SYSTEM.length());
        String fileName = socketExportBlogFileDto.getBlogFileName();
        String deviceFilePath = "/opt/docker/files/temp";
        NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToDevice(serviceFilePath, deviceFilePath);
        nettySyncFileDto.setFileNameList(List.of(fileName));
        sendSyncFileMsg(msgHead, nettySyncFileDto, 1);
    }

    /**
     * 定时任务请求树莓派文件上传
     *
     * @param bo
     * @param msgHead
     */
    public String syncDeviceFile(SyncDeviceFileBo bo, MsgHead msgHead) {
        logger.info("===== 定时任务-树莓派文件上传 ===== SyncDeviceFileBo: {} MsgHead: {}", bo, msgHead);
        Integer userId = bo.getUserId();

        LambdaQueryWrapper<FileCategory> fileCategoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fileCategoryLambdaQueryWrapper.eq(FileCategory::getDirPath, "/" + userId + bo.getMinioPath());
        FileCategory fileCategory = fileCategoryMapper.selectOne(fileCategoryLambdaQueryWrapper);
        if (fileCategory != null) {
            LambdaQueryWrapper<FileCategoryData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileCategoryData::getFileCategoryId, fileCategory.getId());
            int fileCount = fileCategoryDataMapper.selectCount(wrapper).intValue();
            // 当前目录下超过目录下最大待处理文件时 不进行同步
            if (fileCount > bo.getMaxFileCount()) {
                return "当前目录下文件超过最大数量: " + bo.getMaxFileCount();
            }

            // 文件存储minio中路径
            String minioPath = "/" + userId + bo.getMinioPath();
            // servicePath 为文件在ftp system用户目录下的相对路径
            String servicePath = "/temp/" + MyStringUtils.getRandomString(6);
            // devicePath 为树莓派设备上的绝对路径
            String devicePath = bo.getDevicePath();
            NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToService(minioPath, servicePath, devicePath);
            nettySyncFileDto.setCount(bo.getCount());
            sendSyncFileMsg(msgHead, nettySyncFileDto, userId);
        }
        return "消息发送完成";
    }

    /**
     * 定时清理服务器文件
     *
     * @param path    文件路径
     * @param msgHead 消息头
     */
    public String clearTempFileOrPath(String path, MsgHead msgHead) {
        logger.info("===== 定时任务-清理服务器文件 ===== path: {} MsgHead: {}", path, msgHead);
        socketMessageSendService.deleteDir(path);
        return "删除文件: " + path;
    }

    /**
     * 删除临时同步目录文件
     */
    public void deleteTempFile(String filePath, String time) {
        List<Object> taskList = redisService.getList(TaskConstant.TASK_BASE, 0, -1);
        for (Object o : taskList) {
            TaskBase taskBase = (TaskBase) o;
            if (taskBase.getTaskCode().equals(Constant.TASK_DELETE_TEMP_FILE)) {
                // 定时删除同步文件
                TaskEntity taskEntity = new TaskEntity();
                BeanUtils.copyProperties(taskBase, taskEntity);
                taskEntity.setTaskParams(new ArrayList<>(Collections.singletonList(filePath)));
                taskEntity.setTaskTime(time);
                taskEntity.setTaskCount(1);
                createTaskService.createTask(taskEntity);
            }
        }
    }
}
