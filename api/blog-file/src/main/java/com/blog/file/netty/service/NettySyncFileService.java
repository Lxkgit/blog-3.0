package com.blog.file.netty.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.file.device.entity.UserDevice;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.task.del.bo.SyncDeviceFileBo;
import com.blog.core.domain.file.task.del.bo.SyncServiceFileBo;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.utils.DateUtil;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.minio.MinioService;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.domain.enums.NettyTopic;
import com.blog.file.service.FileService;
import com.blog.file.service.TaskLogService;
import com.blog.file.service.UploadFileService;
import com.blog.file.socket.domain.SocketPacket;
import com.blog.file.socket.domain.constant.SocketClientType;
import com.blog.file.socket.domain.constant.SocketConstant;
import com.blog.file.socket.domain.constant.SocketTopic;
import com.blog.file.socket.domain.dto.SocketDeleteFileOrDirDto;
import com.blog.file.socket.domain.dto.SocketExportBlogFileDto;
import com.blog.file.socket.domain.service.SocketMessageSendService;
import com.blog.file.socket.config.SocketService;
import com.blog.file.utils.VideoUtil;
import com.blog.redis.constant.FileRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

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

    @Resource
    private UploadFileService uploadFileService;

    /**
     * 发送文件同步消息至树莓派
     *
     * @param nettySyncFileDto 同步文件参数
     */
    public boolean sendSyncFileMsg(MsgHead msgHead, NettySyncFileDto nettySyncFileDto, Integer userId) {

        // 获取用户默认同步数据设备
        LambdaQueryWrapper<UserDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDevice::getUserId, userId);
        List<UserDevice> deviceList = userDeviceMapper.selectList(wrapper);

        if (CollectionUtils.isNotEmpty(deviceList)) {
            UserDevice device = deviceList.get(0);
            String registerId = device.getDeviceCode();

            NettyPacket<NettySyncFileDto> nettyPacket = NettyPacket.buildRequest(NettyTopic.BLOG_FILE_SYNC, nettySyncFileDto);
            nettyPacket.setMsgHead(msgHead);
            return nettyServer.sendByRegisterIdLimitTime(registerId, nettyPacket.getMsgHead().getNettyMsgHead().getRequestId(),
                    JSON.toJSONString(nettyPacket), 2 * 60);

        }
        return false;
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
                    fileDownloadDevice(nettySyncFileDto, msgHead);
                } else if (nettySyncFileDto.getSyncType() == 2) {

                    // 文件上传消息响应
                    fileUploadService(nettySyncFileDto);

                    // 文件导入minio
                    fileImportMinio(nettySyncFileDto, msgHead);
                }
            }

            // 流程结束，文件下载或上传成功之后删除临时目录
            if (nettySyncFileDto.getSyncEnd() == 1) {
                afterSyncFile(msgHead, nettySyncFileDto);
            }
        }

        // 文件同步 任务请求头不为空时记录任务日志
        if (msgHead != null && msgHead.getTaskMsgHead() != null && StringUtils.isNotEmpty(msgHead.getTaskMsgHead().getTaskUuid())) {

        }
    }

    /**
     * 文件同步流程完成
     *
     * @param msgHead
     * @param nettySyncFileDto
     */
    private void afterSyncFile(MsgHead msgHead, NettySyncFileDto nettySyncFileDto) {
        // 上传文件时，最后一个上传的文件上传完成不一定全部文件都正确导入minio，等待1h文件导入完成
        String time = nettySyncFileDto.getSyncType() == 1 ? "10s" : "1h";
        if (msgHead != null && msgHead.getTaskMsgHead() != null && StrUtil.isNotBlank(msgHead.getTaskMsgHead().getSubTaskUuid())) {
            deleteTempFile(msgHead.getTaskMsgHead().getSubTaskUuid(), Constant.FTP_PATH_SYSTEM + nettySyncFileDto.getServiceFilePath(), time);
        } else {
            deleteTempFile(Constant.FTP_PATH_SYSTEM + nettySyncFileDto.getServiceFilePath(), time);
        }

        // 文件同步任务收到消息后重置发送标识
        if (msgHead != null && msgHead.getTaskMsgHead() != null && nettySyncFileDto.getSyncCount() != null && nettySyncFileDto.getSyncCount() == 2) {
            redisService.setString(FileRedisConstant.FILE_SYNC_TASK_STATUS + msgHead.getTaskMsgHead().getTaskUuid(), "1", 5 * 60 * 60);
        }
    }

    /**
     * 文件下载到树莓派设备
     *
     * @param nettyUploadBlogFileDto
     * @param msgHead
     */
    private void fileDownloadDevice(NettySyncFileDto nettyUploadBlogFileDto, MsgHead msgHead) {
        logger.info("树莓派下载完成 {} 文件", nettyUploadBlogFileDto.getFileCodeList());
        String minioPath = nettyUploadBlogFileDto.getMinioPath();
        if (StringUtils.isEmpty(minioPath)) {
            return;
        }

        // 修改文件同步状态为远程服务器
        if (CollectionUtils.isNotEmpty(nettyUploadBlogFileDto.getFileCodeList())) {
            for (String fileCode : nettyUploadBlogFileDto.getFileCodeList()) {
                Integer id = Integer.valueOf(fileCode.split(":")[0]);
                FileCategoryData fileCategoryData = fileCategoryDataMapper.selectById(id);
                FileCategory fileCategory = fileCategoryMapper.selectById(fileCategoryData.getFileCategoryId());

                // 修改目录下文件状态为远程服务器
                LambdaQueryWrapper<FileCategoryData> dataWrapper = new LambdaQueryWrapper<>();
                dataWrapper.eq(FileCategoryData::getFileCategoryId, fileCode);
                FileCategoryData data = new FileCategoryData();
                data.setFileStatus(4);
                fileCategoryDataMapper.update(data, dataWrapper);

                // 移除minio中文件
                if (nettyUploadBlogFileDto.getMinioDeleteFlag() == 1) {
                    String fileName = minioService.getFileName(fileCategoryData.getFileUrl());
                    minioService.deleteFile(fileCategory.getDirPath(), fileName);
                }
            }
        }
    }

    /**
     * @param nettySyncFileDto
     */
    private void fileUploadService(NettySyncFileDto nettySyncFileDto) {
        if (nettySyncFileDto.getFileSource() != null) {
            if (nettySyncFileDto.getFileSource() == 2) {
                // 系统外部来源的文件需要进行重命名
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
            } else if (nettySyncFileDto.getFileSource() == 1) {
                // 系统内部部来源的文件修改文件状态
                for (String fileCode : nettySyncFileDto.getFileCodeList()) {
                    // 修改目录下文件状态为本地服务器
                    LambdaQueryWrapper<FileCategoryData> dataWrapper = new LambdaQueryWrapper<>();
                    dataWrapper.eq(FileCategoryData::getFileCategoryId, fileCode.split(":")[0]);
                    FileCategoryData data = new FileCategoryData();
                    data.setFileStatus(0);
                    fileCategoryDataMapper.update(data, dataWrapper);
                }
            }
        }
    }

    /**
     * 文件导入minio
     *
     * @param nettyUploadBlogFileDto
     */
    private void fileImportMinio(NettySyncFileDto nettyUploadBlogFileDto, MsgHead msgHead) {
        logger.info("===== 文件导入minio ===== NettySyncFileDto: {} ", nettyUploadBlogFileDto);
        Integer userId = msgHead.getUserId();
        String minioPath = nettyUploadBlogFileDto.getMinioPath();
        if (StringUtils.isEmpty(minioPath)) {
            return;
        }
        Integer categoryId = createDirWithUserId(minioPath);
        for (String fileName : nettyUploadBlogFileDto.getFileNameList()) {

            LambdaQueryWrapper<FileCategoryData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileCategoryData::getFileCategoryId, categoryId);
            wrapper.eq(FileCategoryData::getFileName, fileName);
            FileCategoryData fileCategoryData = fileCategoryDataMapper.selectOne(wrapper);

            String localFilePath = Constant.FTP_PATH_SYSTEM + nettyUploadBlogFileDto.getServiceFilePath() + "/" + fileName;
            if (fileCategoryData == null) {
                // 文件本地存放目录

                // 文件转为 MultipartFile
                File file = new File(localFilePath);

                String fileUrl = minioService.getFileUrl(minioPath, fileName);
                FileCategoryData newFile = new FileCategoryData();
                newFile.setUserId(userId);
                newFile.setFileName(fileName);
                newFile.setFileCategoryId(categoryId);
                newFile.setFileUrl(fileUrl);
                newFile.setFileSize(file.length());
                newFile.setFileStatus(0);
                newFile.setFileType(fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase());
                newFile.setFileJson(VideoUtil.resolveVideo(file));
                newFile.setCreateBy("system");
                newFile.setCreateTime(new Date());

                boolean importFlag = minioService.importFile(localFilePath, minioPath);
                logger.info("系统外部文件导入minio结果: {}", importFlag);
                fileCategoryDataMapper.insert(newFile);

            } else {
                // 文件本地存放目录
                boolean importFlag = minioService.importFile(localFilePath, minioPath);
                logger.info("系统内部文件导入minio结果: {}", importFlag);
                fileCategoryData.setFileStatus(0);
                fileCategoryDataMapper.updateById(fileCategoryData);
            }
        }
    }

    /**
     * 创建目录
     *
     * @param path 目录
     */
    private Integer createDirWithUserId(String path) {
        SecurityUtil.setSystem();
        return uploadFileService.createFileCategory(path, 1);
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
    public String syncBlogDataFirstStep(String blogFilePath, MsgHead msgHead) {
        logger.info("===== 定时任务-博客数据同步-socket导出数据 ===== MsgHead: {}", msgHead);
        TaskLog log = TaskLog.builder().taskName("发送socket消息开始导出博客文件").build();
        taskLogService.executeTaskLog(msgHead, log);
        msgHead.getTaskMsgHead().setLogStepId(log.getId());

        // 构建socket发送消息包
        SocketExportBlogFileDto exportBlogFileDto = new SocketExportBlogFileDto();
        exportBlogFileDto.setBlogFilePath(blogFilePath);
        SocketPacket<SocketExportBlogFileDto> requestPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_EXPORT_BLOG_FILE, msgHead, exportBlogFileDto);

        taskLogService.taskStartLog(log.getId());

        try {
            // 发送socket消息开始导出博客文件
            boolean result = socketService.sendMessage(SocketClientType.PYTHON, SocketConstant.LOCALHOST_REGISTER_CODE, requestPacket);
            taskLogService.taskSuccessLog(log.getId(), JSONObject.toJSONString(exportBlogFileDto));
        } catch (Exception e) {
            taskLogService.taskFailureLog(log.getId(), e);
            throw e;
        }
        return blogFilePath;
    }

    /**
     * 博客数据同步任务-第二步
     * 收到socket消息，组合netty消息，发送到设备
     */
    public void syncBlogDataSecondStep(String data, MsgHead msgHead) {
        logger.info("===== 定时任务-博客数据同步-文件同步树莓派 ===== data：{} MsgHead: {}", data, msgHead);
        // 文件导出日志结束
        taskLogService.taskEndLog(msgHead.getTaskMsgHead().getLogStepId());

        TaskLog log = TaskLog.builder().taskName("发送netty消息开始下载博客文件").build();
        taskLogService.executeTaskLog(msgHead, log);
        msgHead.getTaskMsgHead().setLogStepId(log.getId());

        // 此处将文件在ftp的全路径转换为在ftp/system用户目录下的路径
        SocketExportBlogFileDto socketExportBlogFileDto = JSONObject.parseObject(data, SocketExportBlogFileDto.class);
        String serviceFilePath = socketExportBlogFileDto.getBlogFilePath().substring(Constant.FTP_PATH_SYSTEM.length());
        String fileName = socketExportBlogFileDto.getBlogFileName();
//        String deviceFilePath = Constant.DISK_PATH_BLOG_BAK;
        JSONObject param = JSONObject.parseObject(msgHead.getTaskMsgHead().getTaskParam());
        String deviceFilePath = param.getString("deviceFilePath");

        // 构建netty发送消息包
        NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToDevice(serviceFilePath, deviceFilePath);
        nettySyncFileDto.setFileNameList(List.of(fileName));

        taskLogService.taskStartLog(log.getId());

        // 发送netty消息开始下载博客文件
        boolean result = sendSyncFileMsg(msgHead, nettySyncFileDto, msgHead.getUserId());

        if (result) {
            taskLogService.taskSuccessLog(log.getId(), JSONObject.parseObject(deviceFilePath).toJSONString());
        } else {
            taskLogService.taskFailureLog(log.getId(), null);
        }
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
        redisService.setString(FileRedisConstant.FILE_SYNC_TASK_STATUS + msgHead.getTaskMsgHead().getTaskUuid(), "1", 5 * 60 * 60);
        baseThread.execute(() -> syncServiceFileSend(bo, msgHead));
        return "minio文件同步任务已启动";
    }

    /**
     * 定时同步minio中文件至树莓派设备
     *
     * @param bo
     * @param msgHead
     */
    @SuppressWarnings({"BusyWait"})
    public void syncServiceFileSend(SyncServiceFileBo bo, MsgHead msgHead) {
        // 查询需要同步的目录
        LambdaQueryWrapper<FileCategory> categoryWrapper = new LambdaQueryWrapper<>();
        categoryWrapper.likeRight(FileCategory::getDirPath, bo.getDirPath());
        List<FileCategory> fileCategoryList = fileCategoryMapper.selectList(categoryWrapper);

        // 每个目录依次处理
        for (FileCategory fileCategory : fileCategoryList) {
            // 查询当前目录下存放在服务器的文件列表
            LambdaQueryWrapper<FileCategoryData> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FileCategoryData::getFileCategoryId, fileCategory.getId());
            wrapper.eq(FileCategoryData::getFileStatus, 0);
            List<FileCategoryData> fileCategoryDataList = fileCategoryDataMapper.selectList(wrapper);
            // 开始处理文件同步
            if (CollectionUtils.isNotEmpty(fileCategoryDataList)) {
                // 当前目录下存在文件，每次取一定数量文件进行判断是否需要同步
                int batchSize = 10;
                // 文件分批次同步 每次10个文件
                for (int i = 0; i < fileCategoryDataList.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, fileCategoryDataList.size());
                    // 获取本次同步文件名称
                    List<FileCategoryData> sendList = fileCategoryDataList.subList(i, end);

                    // 生成随机目录 文件由minio导出至ftp中此目录中
                    String serviceFilePath = "/temp/" + MyStringUtils.getRandomString(6);
                    exportMinioFileList(sendList, fileCategory.getDirPath(), Constant.FTP_PATH_SYSTEM + serviceFilePath);

                    // 指定树莓派存放文件目录 除前缀地址外 其余地址与服务器一致
                    String deviceFilePath = Constant.DISK_PATH_BLOG_MINIO + fileCategory.getDirPath();

                    // 构建netty消息请求
                    NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToDevice(serviceFilePath, deviceFilePath);
                    List<String> fileNameList = new ArrayList<>();
                    for (FileCategoryData file : sendList) {
                        String fileUrl = file.getFileUrl();
                        // 设置文件上传下载名称 （文件实际名称不一定与fileName字段一致，取url中文件为准）
                        fileNameList.add(fileUrl.substring(fileUrl.lastIndexOf("/") + 1));
                    }
                    nettySyncFileDto.setCheckFile(1);
                    nettySyncFileDto.setSyncCount(2);
                    nettySyncFileDto.setFileNameList(fileNameList);

                    // 任务扫描次数
                    int waitCount = 300 * 12;
                    // 任务扫描时间（秒）
                    int scanTime = 5;
                    int localCount = 0;
                    while (true) {
                        if (localCount > waitCount) {
                            logger.info("===== 定时任务-云盘文件同步-超出最大等待时间: {} 秒 ===== taskUUID: {}", waitCount * scanTime, msgHead.getTaskMsgHead().getTaskUuid());
                            return;
                        }
                        // 文件同步状态标识存放redis中 当树莓派设备下载完成之后 会将此状态设置为 1，然后开始下一轮循环
                        String status = redisService.getString(FileRedisConstant.FILE_SYNC_TASK_STATUS + msgHead.getTaskMsgHead().getTaskUuid()).toString();
                        if (StringUtils.isNotEmpty(status) && "1".equals(status)) {
                            MsgHead head = new MsgHead();
                            msgHead.getTaskMsgHead().setSubTaskUuid(msgHead.getTaskMsgHead().getTaskUuid() + "-" + "clear");
                            BeanUtils.copyProperties(msgHead, head);
                            // 发送文件同步命令
                            sendSyncFileMsg(head, nettySyncFileDto, 1);
                            // 发送后文件同步状态设为0等待树莓派下载数据
                            redisService.setString(FileRedisConstant.FILE_SYNC_TASK_STATUS + msgHead.getTaskMsgHead().getTaskUuid(), "0", waitCount * scanTime);
                            break;
                        } else {
                            try {
                                // 最大等待时间为 300 分钟
                                localCount++;
                                Thread.sleep(scanTime * 1000);
                            } catch (Exception e) {
                                // 等待树莓派响应超时
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                    logger.info("===== 定时任务-文件同步完成 ===== 消息监听任务结束");
                }
            }
            // 文件同步完成后，清除指定目录下文件
            if (CollectionUtils.isNotEmpty(bo.getClearPath()) && bo.getClearPath().contains(fileCategory.getDirPath())) {
                logger.info("===== 定时任务-文件同步完成 ===== 清理 服务器与minio 目录: {} 下文件: ", bo.getClearPath());
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
        logger.info("===== 定时任务-云盘文件同步结束 ===== taskUUID: {}", msgHead.getTaskMsgHead().getTaskUuid());
    }

    /**
     * 导出minio中文件至服务器指定位置
     *
     * @param fileCategoryDataList 需要导出的minio文件名称列表
     * @param minioPath            minio中文件路径
     * @param localPath            服务器文件路径
     */
    private void exportMinioFileList(List<FileCategoryData> fileCategoryDataList, String minioPath, String localPath) {
        logger.info("===== minio 导出文件 ===== minioPath:{} localPath:{} fileList:{}", minioPath, localPath, fileCategoryDataList);
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
        return "清理文件(目录): " + path;
    }

    /**
     * 删除临时同步目录文件
     *
     * @param filePath 文件目录
     * @param time     删除操作延迟时间
     */
    public void deleteTempFile(String filePath, String time) {
//        List<Object> taskList = redisService.getList(TaskConstant.TASK_BASE, 0, -1);
//        for (Object o : taskList) {
//            TaskBase taskBase = (TaskBase) o;
//            if (taskBase.getTaskCode().equals(Constant.TASK_DELETE_TEMP_FILE)) {
//                // 定时删除同步文件
//                TaskEntity taskEntity = new TaskEntity();
//                BeanUtils.copyProperties(taskBase, taskEntity);
//                taskEntity.setTaskParams(new ArrayList<>(Collections.singletonList(filePath)));
//                taskEntity.setTaskTime(time);
//                taskEntity.setTaskCount(1);
//                createTaskService.createTask(taskEntity);
//            }
//        }
    }

    /**
     * 删除临时同步目录文件
     *
     * @param filePath 文件目录
     * @param time     删除操作延迟时间
     */
    public void deleteTempFile(String taskUUID, String filePath, String time) {
//        List<Object> taskList = redisService.getList(TaskConstant.TASK_BASE, 0, -1);
//        for (Object o : taskList) {
//            TaskBase taskBase = (TaskBase) o;
//            if (taskBase.getTaskCode().equals(Constant.TASK_DELETE_TEMP_FILE)) {
//                // 定时删除同步文件
//                TaskEntity taskEntity = new TaskEntity();
//                taskEntity.setTaskUUID(taskUUID);
//                BeanUtils.copyProperties(taskBase, taskEntity);
//                taskEntity.setTaskParams(new ArrayList<>(Collections.singletonList(filePath)));
//                taskEntity.setTaskTime(time);
//                taskEntity.setTaskCount(1);
//                createTaskService.createTask(taskEntity);
//            }
//        }
    }

    /**
     * socket删除文件响应消息
     *
     * @param dto
     * @param msgHead
     */
    public void receiveSocketDeleteFileMsg(SocketDeleteFileOrDirDto dto, MsgHead msgHead) {
        // 文件同步 任务请求头不为空时记录任务日志
        if (msgHead != null && msgHead.getTaskMsgHead() != null && StringUtils.isNotEmpty(msgHead.getTaskMsgHead().getTaskUuid())) {
//            taskLogService.recordDelFileTaskLog(dto, msgHead);
        }

        if (StringUtils.isNotEmpty(dto.getFileName())) {
            logger.info("文件删除结果 result:{} path: {} fileName: {}", dto.getResult(), dto.getDirPath(), dto.getFileName());
        } else {
            logger.info("文件删除结果 result:{} path: {}", dto.getResult(), dto.getDirPath());
        }
    }
}
