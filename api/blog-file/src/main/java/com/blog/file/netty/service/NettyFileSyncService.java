package com.blog.file.netty.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.constant.Constant;
import com.blog.core.domain.file.device.entity.UserDevice;
import com.blog.core.domain.file.task.bo.SyncDeviceFileBo;
import com.blog.core.utils.MyStringUtils;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.domain.dto.file.NettyUploadBlogFileDto;
import com.blog.file.netty.domain.enums.NettyTopic;
import com.blog.file.service.FileService;
import com.blog.file.socket.domain.SocketPacket;
import com.blog.file.socket.domain.constant.SocketClientType;
import com.blog.file.socket.domain.constant.SocketConstant;
import com.blog.file.socket.domain.constant.SocketTopic;
import com.blog.file.socket.domain.dto.SocketExportBlogFileDto;
import com.blog.file.socket.domain.service.SocketMessageSendService;
import com.blog.file.socket.service.SocketService;
import com.blog.redis.service.RedisService;
import com.blog.task.constant.TaskConstant;
import com.blog.task.domain.TaskBase;
import com.blog.task.domain.TaskEntity;
import com.blog.task.service.CreateTaskService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: Netty文件同步业务
 * @Author: lxk
 * @date 2024/3/11 11:41
 */

@Service
public class NettyFileSyncService {

    private static final Logger logger = LoggerFactory.getLogger(NettyFileSyncService.class);

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

    /**
     * netty消息发送文件同步到服务器
     *
     * @param nettySyncFileDto 同步文件参数
     */
    public void syncFileSend(NettySyncFileDto nettySyncFileDto, Integer userId) {

        // 获取用户默认同步数据设备
        LambdaQueryWrapper<UserDevice> userDeviceLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userDeviceLambdaQueryWrapper.eq(UserDevice::getUserId, userId);
        List<UserDevice> deviceList = userDeviceMapper.selectList(userDeviceLambdaQueryWrapper);

        if (CollectionUtils.isNotEmpty(deviceList)) {
            UserDevice device = deviceList.get(0);
            String registerId = device.getDeviceCode();

            NettyPacket<NettySyncFileDto> nettyPacket = NettyPacket.buildRequest(NettyTopic.BLOG_FILE_SYNC, nettySyncFileDto);
            nettyServer.sendByRegisterIdLimitTime(registerId, JSON.toJSONString(nettyPacket), 8 * 60);
        }
//        deleteTempFile(nettySyncFileDto.getServiceFilePath());
    }

    /**
     *  删除临时同步目录文件
     */
    public void deleteTempFile(String filePath, String time) {
        List<Object> taskList = redisService.getList(TaskConstant.TASK_BASE, 0, -1);
        for (Object o : taskList) {
            TaskBase taskBase = (TaskBase) o;
            if (taskBase.getTaskCode().equals(Constant.TASK_DELETE_TEMP_FILE)) {
                // 定时删除同步文件
                TaskEntity taskEntity = new TaskEntity();
                BeanUtils.copyProperties(taskBase, taskEntity);
                taskEntity.setTaskParams(new Object[]{filePath});
                taskEntity.setTaskTime(time);
                taskEntity.setTaskCount(1);
                createTaskService.createTask(taskEntity);
            }
        }
    }

    /**
     * 博客数据同步任务-第一步
     * 发送socket导出博客数据任务
     */
    public String syncBlogDataFirstStep() {
        logger.info("正在导出博客文件数据");
        SocketExportBlogFileDto exportBlogFileDto = new SocketExportBlogFileDto();
        String blogFilePath = Constant.FTP_PATH_SYSTEM + "/temp/" + MyStringUtils.getRandomString(6);
        exportBlogFileDto.setBlogFilePath(blogFilePath);
        SocketPacket<SocketExportBlogFileDto> requestPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_EXPORT_BLOG_FILE, exportBlogFileDto);
        socketService.sendMessage(SocketClientType.PYTHON, SocketConstant.LOCALHOST_REGISTER_CODE, requestPacket);
        deleteTempFile(blogFilePath, "48h");
        return blogFilePath;
    }

    /**
     * 博客数据同步任务-第二步
     * 收到socket消息，组合netty消息，发送到设备
     */
    public void syncBlogDataSecondStep(String data) {

        SocketExportBlogFileDto socketExportBlogFileDto = JSONObject.parseObject(data, SocketExportBlogFileDto.class);
        // 此处将文件在ftp的全路径转换为在ftp/system用户目录下的路径
        String serviceFilePath = socketExportBlogFileDto.getBlogFilePath().substring(Constant.FTP_PATH_SYSTEM.length());
        String fileName = socketExportBlogFileDto.getBlogFileName();
        String deviceFilePath = "/opt/docker/files/temp";

        NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToDevice(serviceFilePath, deviceFilePath);
        nettySyncFileDto.setFileNameList(List.of(fileName));
        syncFileSend(nettySyncFileDto, 1);
    }

    public void syncDeviceFile(SyncDeviceFileBo bo) {
        Integer userId = 1;
        // 文件存储minio中路径
        String minioPath = "/"+ userId + bo.getMinioPath();
        // servicePath 为文件在ftp system用户目录下的相对路径
        String servicePath = "/temp/" + MyStringUtils.getRandomString(6);
        // devicePath 为树莓派设备上的绝对路径
        String devicePath = bo.getDevicePath();
        NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToService(minioPath, servicePath, devicePath);
        nettySyncFileDto.setCount(bo.getCount());
        syncFileSend(nettySyncFileDto, userId);
    }

    /**
     * netty响应消息处理同步到服务器
     *
     * @param nettyUploadBlogFileDto
     * @param deviceCode
     * @param userId
     */
    public void syncFileReceive(NettyUploadBlogFileDto nettyUploadBlogFileDto, String deviceCode, Integer userId) {
        if (nettyUploadBlogFileDto.getSyncResult().equals(0)) {

        } else if (nettyUploadBlogFileDto.getSyncResult().equals(1)) {
            // 文件传输都是使用ftp system用户下相对路径
            socketMessageSendService.deleteDir(Constant.FTP_PATH_SYSTEM + nettyUploadBlogFileDto.getServiceFilePath());
        } else if (nettyUploadBlogFileDto.getSyncResult().equals(2)) {
            fileService.fileImportMinio(nettyUploadBlogFileDto);
        }
    }

    /**
     *
     */
    public void clearTempFileOrPath(String path) {
        logger.info("清理文件：{}", path);
        socketMessageSendService.deleteDir(path);
    }

}
