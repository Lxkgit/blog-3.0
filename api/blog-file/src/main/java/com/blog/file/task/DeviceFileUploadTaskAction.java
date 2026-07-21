package com.blog.file.task;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.file.files.entity.FileCategory;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import com.blog.core.domain.netty.dto.file.NettySyncFileDto;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.netty.head.TaskMsgHead;
import com.blog.core.domain.file.task.del.bo.SyncDeviceFileBo;
import com.blog.core.utils.MyStringUtils;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.file.mapper.FileCategoryMapper;
import com.blog.timer.context.TimerTaskContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Description  树莓派文件
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class DeviceFileUploadTaskAction extends SystemTimerAction {

    private static final Logger logger = LoggerFactory.getLogger(DeviceFileUploadTaskAction.class);

    @Resource
    private FileCategoryMapper fileCategoryMapper;

    @Resource
    private FileCategoryDataMapper fileCategoryDataMapper;

    @Override
    public String getCode() {
        return Constant.TASK_SYNC_DEVICE_FILE;
    }

    @Override
    public String getName() {
        return "定时上传树莓派数据";
    }

    @Override
    public String doExecute(TimerTaskContext context, TaskMsgHead taskMsgHead) {
        SyncDeviceFileBo bo = JSONObject.parseObject(context.get("param"), SyncDeviceFileBo.class);
        bo.setUserId(taskMsgHead.getUserId());
        taskMsgHead.setTaskParam(context.get("param"));
        return syncDeviceFile(bo, MsgHead.buildTaskMsgHead(taskMsgHead.getUserId(), taskMsgHead));
    }

    @Override
    public String getParamTemplate() {
        JSONArray array = new JSONArray();

        JSONObject minioPath = new JSONObject();
        minioPath.put("type", "input");
        minioPath.put("name", "minio文件路径");
        minioPath.put("paramName", "minioPath");
        minioPath.put("length", "200");
        array.add(minioPath);

        JSONObject devicePath = new JSONObject();
        devicePath.put("type", "input");
        devicePath.put("name", "设备文件路径");
        devicePath.put("paramName", "devicePath");
        devicePath.put("length", "200");
        array.add(devicePath);

        JSONObject count = new JSONObject();
        count.put("type", "input-number");
        count.put("name", "同步文件数量");
        count.put("paramName", "count");
        count.put("min", 0);
        count.put("max", 50);
        array.add(count);

        JSONObject maxFileCount = new JSONObject();
        maxFileCount.put("type", "input-number");
        maxFileCount.put("name", "目录下最大文件数量");
        maxFileCount.put("paramName", "maxFileCount");
        maxFileCount.put("min", 0);
        maxFileCount.put("max", 200);
        array.add(maxFileCount);

        return array.toString();
    }

    /**
     * 定时任务请求树莓派文件上传
     *
     * @param bo 文件同步擦拭
     * @param msgHead 消息头
     * @return 响应消息
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

}
