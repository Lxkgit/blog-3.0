package com.blog.file.netty.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.utils.DateUtil;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.minio.MinioService;
import com.blog.core.domain.netty.dto.file.NettySyncFileDto;
import com.blog.file.service.TaskLogService;
import com.blog.file.service.UploadFileService;
import com.blog.core.domain.socket.dto.SocketDeleteFileOrDirDto;
import com.blog.file.utils.VideoUtil;
import com.blog.redis.constant.FileRedisConstant;
import com.blog.redis.service.RedisService;
import com.blog.timer.action.TimerActionManager;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTaskDefinition;
import com.blog.timer.entity.policy.Policy;
import com.blog.timer.entity.trigger.DelayTrigger;
import com.blog.timer.manager.TimerManager;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: Netty文件同步业务
 * @Author: lxk
 * @date 2024/3/11 11:41
 */

@Service
public class NettySyncFileReceiveService {

    private static final Logger logger = LoggerFactory.getLogger(NettySyncFileReceiveService.class);

    @Resource
    private RedisService redisService;

    @Resource
    private TaskLogService taskLogService;

    @Resource
    private FileCategoryMapper fileCategoryMapper;

    @Resource
    private FileCategoryDataMapper fileCategoryDataMapper;

    @Resource
    private MinioService minioService;

    @Resource
    private UploadFileService uploadFileService;

    @Resource
    private TimerManager timerManager;

    @Resource
    private TimerActionManager timerActionManager;

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

        // 任务中触发文件交互流程记录任务日志
        recordTaskLog(msgHead, nettySyncFileDto);
    }

    /**
     * 文件下载到树莓派设备
     *
     * @param nettyUploadBlogFileDto
     * @param msgHead
     */
    private void fileDownloadDevice(NettySyncFileDto nettyUploadBlogFileDto, MsgHead msgHead) {
        logger.info("树莓派下载完成 {} 文件", nettyUploadBlogFileDto.getFileNameList());
        String minioPath = nettyUploadBlogFileDto.getMinioPath();
        if (StringUtils.isEmpty(minioPath)) {
            return;
        }

        // 修改文件同步状态为远程服务器
        if (CollectionUtils.isNotEmpty(nettyUploadBlogFileDto.getFileNameList())) {
            for (String fileCode : nettyUploadBlogFileDto.getFileNameList()) {
                // 下载到远程服务器的文件一定是系统内文件 名称格式中有id
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
                for (String fileCode : nettySyncFileDto.getFileNameList()) {
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
     * 文件同步流程完成
     *
     * @param msgHead
     * @param nettySyncFileDto
     */
    private void afterSyncFile(MsgHead msgHead, NettySyncFileDto nettySyncFileDto) {
        logger.info("文件同步流程完成: MsgHead: {} NettySyncFileDto: {}", msgHead, nettySyncFileDto);
        // 上传文件时，最后一个上传的文件上传完成不一定全部文件都正确导入minio，等待1h文件导入完成
        clearFile(Constant.FTP_PATH_SYSTEM + nettySyncFileDto.getServiceFilePath());

        // 文件同步任务收到消息后重置发送标识
        if (msgHead != null && msgHead.getTaskMsgHead() != null && nettySyncFileDto.getSyncCount() != null && nettySyncFileDto.getSyncCount() == 2) {
            redisService.setString(FileRedisConstant.FILE_SYNC_TASK_STATUS + msgHead.getTaskMsgHead().getTaskUuid(), "1", 60);
        }
    }

    /**
     * 创建清理文件任务
     *
     * @param path 文件或目录全路径
     */
    private void clearFile(String path) {
        logger.info("创建文件清理任务: {}", path);
        TimerTaskContext context = new TimerTaskContext();
        context.put("deleteFilePath", path);
        TimerTaskDefinition definition = TimerTaskDefinition.builder()
                .taskCode(Constant.TASK_DELETE_TEMP_FILE)
                .action(timerActionManager.get(Constant.TASK_DELETE_TEMP_FILE))
                .context(context)
                .policy(new Policy(1))
                .trigger(new DelayTrigger(Duration.ofHours(1)))
                .build();
        timerManager.schedule(definition).getTaskId();
    }

    /**
     * 记录任务触发的文件同步日志
     *
     * @param msgHead
     * @param nettySyncFileDto
     */
    private void recordTaskLog(MsgHead msgHead, NettySyncFileDto nettySyncFileDto) {
        // 文件同步 任务请求头不为空时记录任务日志
        if (msgHead != null && msgHead.getTaskMsgHead() != null && StringUtils.isNotEmpty(msgHead.getTaskMsgHead().getTaskUuid())) {
            if (nettySyncFileDto.getResultType() == 1) {
                if (msgHead.getTaskMsgHead().getLogStepId() != null) {
                    taskLogService.taskEndLog(msgHead.getTaskMsgHead().getLogStepId());
                }
            } else if (nettySyncFileDto.getResultType() == 2) {

                // 获取下载/上传文件名称
                String fileNameList = nettySyncFileDto.getFileNameList().toString();
                Integer syncResult = nettySyncFileDto.getSyncResult();
                Integer syncType = nettySyncFileDto.getSyncType();

                // 日志响应内容
                String taskName = syncType == 1 ? "下载文件" : "上传文件";
                String taskResult = "文件" + (syncType == 1 ? "下载" : "上传") + (syncResult == 1 ? "成功" : "失败") + ": " + fileNameList;

                TaskLog taskLog = TaskLog.builder()
                        .taskName(taskName)
                        .taskResult(taskResult)
                        .taskResultStatus(syncResult)
                        .errorMsg(nettySyncFileDto.getErrorMsg())
                        .taskCode(msgHead.getTaskMsgHead().getTaskCode())
                        .taskUuid(msgHead.getTaskMsgHead().getTaskUuid())
                        .build();

                taskLogService.completeTaskLog(taskLog);

                // 修改任务创建日志结束时间
                taskLogService.taskEndLog(msgHead.getTaskMsgHead().getTaskUuid());
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
