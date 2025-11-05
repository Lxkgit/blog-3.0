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
import com.blog.core.domain.file.task.bo.SyncServiceFileBo;
import com.blog.core.utils.DateUtil;
import com.blog.core.utils.MyStringUtils;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.minio.MinioService;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.domain.enums.NettyTopic;
import com.blog.file.service.FileService;
import com.blog.file.socket.domain.SocketPacket;
import com.blog.file.socket.domain.constant.SocketClientType;
import com.blog.file.socket.domain.constant.SocketConstant;
import com.blog.file.socket.domain.constant.SocketTopic;
import com.blog.file.socket.domain.dto.SocketDeleteFileOrDirDto;
import com.blog.file.socket.domain.dto.SocketExportBlogFileDto;
import com.blog.file.socket.domain.service.SocketMessageSendService;
import com.blog.file.socket.service.SocketService;
import com.blog.file.task.TaskLogService;
import com.blog.redis.constant.FileRedisConstant;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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

    @Resource
    private Executor baseThread;

    @Resource
    private MinioService minioService;

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
            if (nettySyncFileDto.getSyncResult() == 1) {
                if (nettySyncFileDto.getSyncType() == 1) {
                    // 文件下载消息响应

                    fileService.fileDownloadDevice(nettySyncFileDto, msgHead);
                } else if (nettySyncFileDto.getSyncType() == 2) {
                    // 文件上传消息响应

                    // 系统外部来源的文件需要进行重命名
                    if (nettySyncFileDto.getFileSource() != null && nettySyncFileDto.getFileSource() == 2) {
                        String filePath = Constant.FTP_PATH_SYSTEM + nettySyncFileDto.getServiceFilePath();
                        List<String> fileNameList = nettySyncFileDto.getFileNameList();
                        List<String> newFileNameList = new ArrayList<>();
                        for (String fileName : fileNameList) {
                            String newFileName = DateUtil.formatDateTimeNoSpaces() + "_" + fileName;
                            if (renameLocalFile(filePath, fileName, filePath, newFileName)) {
                                newFileNameList.add(newFileName);
                            } else {
                                newFileNameList.add(fileName);
                            }
                        }
                        nettySyncFileDto.setFileNameList(newFileNameList);
                    }

                    // 文件导入minio
                    fileService.fileImportMinio(nettySyncFileDto, msgHead);
                }
            }

            // 流程结束，文件下载或上传成功之后删除临时目录
            if (nettySyncFileDto.getSyncEnd() == 1) {
                deleteTempFile(Constant.FTP_PATH_SYSTEM + nettySyncFileDto.getServiceFilePath(), "1h");
            }

            // 文件同步任务收到消息后重置发送标识
            if (nettySyncFileDto.getSyncCount() != null && nettySyncFileDto.getSyncCount() == 2) {
                redisService.setString(FileRedisConstant.FILE_SYNC_TASK_STATUS + msgHead.getTaskMsgHead().getTaskUUID(), "1");
            }
        }

        // 文件同步 任务请求头不为空时记录任务日志
        if (msgHead != null && msgHead.getTaskMsgHead() != null && StringUtils.isNotEmpty(msgHead.getTaskMsgHead().getTaskUUID())) {
            taskLogService.recordSyncFileTaskLog(nettySyncFileDto, msgHead);
        }
    }

    /**
     * 重命名本地文件
     *
     * @param sourceDir  原文件所在目录，例如 "C:/upload/files" 或 "/data/files"
     * @param sourceName 原文件名，例如 "test.txt"
     * @param targetDir  新文件所在目录（可与原目录相同）
     * @param targetName 新文件名，例如 "test_rename.txt"
     * @return 重命名是否成功
     */
    public boolean renameLocalFile(String sourceDir, String sourceName, String targetDir, String targetName) {
        Path sourcePath = Paths.get(sourceDir, sourceName);
        Path targetPath = Paths.get(targetDir, targetName);

        try {
            // 检查原文件是否存在
            if (!Files.exists(sourcePath)) {
                logger.warn("源文件不存在: {}", sourcePath);
                return false;
            }

            // 创建目标目录（如果不存在）
            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }

            // 执行重命名（支持跨目录移动）
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("文件重命名成功: {} → {}", sourcePath, targetPath);
            return true;

        } catch (IOException e) {
            logger.error("文件重命名失败: {} → {}", sourcePath, targetPath, e);
            return false;
        }
    }


    /**
     * 博客数据同步任务-第一步
     * 发送socket导出博客数据任务
     */
    public String syncBlogDataFirstStep(MsgHead msgHead) {
        logger.info("===== 定时任务-博客数据同步-socket导出数据 ===== MsgHead: {}", msgHead);
        SocketExportBlogFileDto exportBlogFileDto = new SocketExportBlogFileDto();
        String blogFilePath = Constant.FTP_PATH_SYSTEM_TEMP + "/" + MyStringUtils.getRandomString(6);
        exportBlogFileDto.setBlogFilePath(blogFilePath);

        SocketPacket<SocketExportBlogFileDto> requestPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_EXPORT_BLOG_FILE,
                msgHead, exportBlogFileDto);
        socketService.sendMessage(SocketClientType.PYTHON, SocketConstant.LOCALHOST_REGISTER_CODE, requestPacket);
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
        String deviceFilePath = Constant.DISK_PATH_BLOG_BAK;
        NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToDevice(serviceFilePath, deviceFilePath);
        nettySyncFileDto.setFileNameList(List.of(fileName));
        sendSyncFileMsg(msgHead, nettySyncFileDto, 1);
    }

    /**
     * 定时任务同步博客云盘文件
     *
     * @param bo
     * @param msgHead
     * @return
     */
    public String syncServiceFile(SyncServiceFileBo bo, MsgHead msgHead) {
        logger.info("===== 定时任务-云盘文件同步 ===== SyncServiceFileBo: {} MsgHead: {}", bo, msgHead);
        redisService.setString(FileRedisConstant.FILE_SYNC_TASK_STATUS + msgHead.getTaskMsgHead().getTaskUUID(), "1", 60 * 60);
        baseThread.execute(() -> syncServiceFileSend(bo, msgHead));
        return "minio文件同步任务已启动";
    }

    public void syncServiceFileSend(SyncServiceFileBo bo, MsgHead msgHead) {
        String sendTaskUUID = msgHead.getTaskMsgHead().getTaskUUID();
        String receiveTaskUUID = msgHead.getTaskMsgHead().getTaskUUID();

        LambdaQueryWrapper<FileCategory> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.likeRight(FileCategory::getDirPath, "/1/user/data/img");
        List<FileCategory> fileCategoryList = fileCategoryMapper.selectList(categoryWrapper);
        for (FileCategory fileCategory : fileCategoryList) {
            LambdaQueryWrapper<FileCategoryData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileCategoryData::getFileCategoryId, fileCategory.getId());
            wrapper.eq(FileCategoryData::getFileStatus, 0);
            List<FileCategoryData> fileCategoryDataList = fileCategoryDataMapper.selectList(wrapper);
            if (CollectionUtils.isNotEmpty(fileCategoryDataList)) {
                // 当前目录下存在文件，每次取一定数量文件进行判断是否需要同步
                int batchSize = 10;
                for (int i = 0; i < fileCategoryDataList.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, fileCategoryDataList.size());
                    List<FileCategoryData> sendList = fileCategoryDataList.subList(i, end);

                    String serviceFilePath = "/temp/" + MyStringUtils.getRandomString(6);
                    exportMinioFileList(sendList, fileCategory.getDirPath(), Constant.FTP_PATH_SYSTEM + serviceFilePath);

                    String deviceFilePath = Constant.DISK_PATH_BLOG_MINIO + fileCategory.getDirPath();
                    NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToDevice(serviceFilePath, deviceFilePath);
                    List<String> fileNameList = new ArrayList<>();
                    for (FileCategoryData file : sendList) {
                        String fileUrl = file.getFileUrl();
                        fileNameList.add(fileUrl.substring(fileUrl.lastIndexOf("/") + 1));
                    }
                    nettySyncFileDto.setCheckFile(1);
                    nettySyncFileDto.setSyncCount(2);
                    nettySyncFileDto.setFileNameList(fileNameList);

                    // 任务最大等待时间
                    int waitCount = 180;
                    int localCount = 0;
                    while (true) {
                        if (localCount > waitCount) {
                            return;
                        }
                        String status = redisService.getString(FileRedisConstant.FILE_SYNC_TASK_STATUS + receiveTaskUUID).toString();
                        if (StringUtils.isNotEmpty(status) && status.equals("1")) {
                            MsgHead head = new MsgHead();
                            BeanUtils.copyProperties(msgHead, head);
                            sendSyncFileMsg(head, nettySyncFileDto, 1);
                            redisService.setString(FileRedisConstant.FILE_SYNC_TASK_STATUS + msgHead.getTaskMsgHead().getTaskUUID(), "0",  60 * 60);
                            receiveTaskUUID = sendTaskUUID;
                            sendTaskUUID = UUID.randomUUID().toString().replace("-", "");
                            msgHead.getTaskMsgHead().setTaskUUID(sendTaskUUID);
                            break;
                        } else {
                            try {
                                localCount++;
                                Thread.sleep(60 * 1000);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            }
            // 文件同步完成后，清除指定目录下文件
            if (CollectionUtils.isNotEmpty(bo.getClearPath()) && bo.getClearPath().contains(fileCategory.getDirPath())) {
                // 修改目录下文件状态为远程服务器
                LambdaQueryWrapper<FileCategoryData> dataWrapper = new LambdaQueryWrapper<>();
                dataWrapper.eq(FileCategoryData::getFileCategoryId, fileCategory.getId());
                FileCategoryData data = new FileCategoryData();
                data.setFileStatus(4);
                fileCategoryDataMapper.update(data, dataWrapper);
                // 移除minio中文件
                minioService.deleteFileByPath(fileCategory.getDirPath());
            }
        }
    }

    /**
     * 导出minio中文件至服务器指定位置
     *
     * @param fileCategoryDataList 需要导出的minio文件名称列表
     * @param minioPath            minio中文件路径
     * @param localPath            服务器文件路径
     */
    private void exportMinioFileList(List<FileCategoryData> fileCategoryDataList, String minioPath, String localPath) {
        if (CollectionUtils.isNotEmpty(fileCategoryDataList)) {
            for (FileCategoryData fileCategoryData : fileCategoryDataList) {
                String fileUrl = fileCategoryData.getFileUrl();
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                minioService.exportFile(minioPath + "/" + fileName, localPath);
            }
        }
    }

    /**
     * 定时任务请求树莓派文件上传
     *
     * @param bo
     * @param msgHead
     * @return
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
            nettySyncFileDto.setFileSource(2);
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
        socketMessageSendService.deleteDir(path, msgHead);
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

    /**
     * socket删除文件响应消息
     *
     * @param dto
     * @param msgHead
     */
    public void receiveSocketDeleteFileMsg(SocketDeleteFileOrDirDto dto, MsgHead msgHead) {
        // 文件同步 任务请求头不为空时记录任务日志
        if (msgHead != null && msgHead.getTaskMsgHead() != null && StringUtils.isNotEmpty(msgHead.getTaskMsgHead().getTaskUUID())) {
            taskLogService.recordDelFileTaskLog(dto, msgHead);
        }

        if (StringUtils.isNotEmpty(dto.getFileName())) {
            logger.info("文件删除结果 result:{} path: {} fileName: {}", dto.getResult(), dto.getDirPath(), dto.getFileName());
        } else {
            logger.info("文件删除结果 result:{} path: {}", dto.getResult(), dto.getDirPath());
        }
    }
}
