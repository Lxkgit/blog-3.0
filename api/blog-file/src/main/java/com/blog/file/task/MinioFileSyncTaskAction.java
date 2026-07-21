package com.blog.file.task;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.file.task.del.bo.SyncDeviceFileBo;
import com.blog.core.domain.netty.dto.file.NettySyncFileDto;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.netty.head.TaskMsgHead;
import com.blog.core.domain.file.task.del.bo.SyncServiceFileBo;
import com.blog.core.utils.MyStringUtils;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.file.minio.MinioService;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.redis.constant.FileRedisConstant;
import com.blog.redis.service.RedisService;
import com.blog.timer.context.TimerTaskContext;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @Description minio文件数据同步
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class MinioFileSyncTaskAction extends SystemTimerAction {

    private static final Logger logger = LoggerFactory.getLogger(MinioFileSyncTaskAction.class);

    @Resource
    private RedisService redisService;

    @Resource
    private Executor baseThread;

    @Resource
    private FileCategoryMapper fileCategoryMapper;

    @Resource
    private FileCategoryDataMapper fileCategoryDataMapper;

    @Resource
    private MinioService minioService;

    @Override
    public String getCode() {
        return Constant.TASK_SYNC_MINIO_FILE;
    }

    @Override
    public String getName() {
        return "minio文件数据同步";
    }

    @Override
    public String doExecute(TimerTaskContext context, TaskMsgHead taskMsgHead) {
        SyncServiceFileBo bo = JSONObject.parseObject(context.get("param"), SyncServiceFileBo.class);
        bo.setUserId(taskMsgHead.getUserId());
        taskMsgHead.setTaskParam(context.get("param"));
        return syncServiceFile(bo, MsgHead.buildTaskMsgHead(taskMsgHead.getUserId(), taskMsgHead));
    }

    @Override
    public String getParamTemplate() {
        JSONArray array = new JSONArray();

        JSONObject minioPath = new JSONObject();
        minioPath.put("type", "input");
        minioPath.put("name", "minio同步目录");
        minioPath.put("paramName", "minioPath");
        minioPath.put("length", "200");
        array.add(minioPath);

        return array.toString();
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
}
