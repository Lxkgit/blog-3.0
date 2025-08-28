package com.blog.file.init;

import com.blog.file.netty.service.NettyFileSyncService;
import com.blog.file.socket.service.SocketService;
import com.blog.task.domain.TaskEntity;
import com.blog.task.service.CreateTaskService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TaskInit implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(TaskInit.class);

    @Resource
    private CreateTaskService createTaskService;

    @Resource
    private SocketService socketService;

    @Resource
    private NettyFileSyncService nettyFileSyncService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("启动系统任务");
        blogDateSyncTask();
    }

    /**
     * 文件同步任务-定时备份博客数据
     * 服务器端：
     * 1. 通知socket执行shell脚本，备份服务器上数据
     * 2. 等待socket备份完成数据，获取备份文件数据地址
     * 3. 发送netty文件同步消息请求
     * 设备端：
     * 1. 收到netty文件同步请求，回复消息响应
     * 2. 启动ftp下载指定目录下备份文件数据
     * 3. ftp下载完成，调用socket将备份文件移动到指定位置
     * 4. socket移动文件完成，netty再次回应消息，响应同步数据成功
     */
    public void blogDateSyncTask() {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setTaskUUID("blog-system-task-sync-blog-file");

        taskEntity.setClazz(NettyFileSyncService.class);
        taskEntity.setMethodName("syncBlogDataFirstStep");
        taskEntity.setParams(null);
        taskEntity.setParamsClazz(null);

        taskEntity.setTaskName("定时备份博客数据");
        taskEntity.setCount(-1);
        taskEntity.setCron("0 0 0 * * *");

        createTaskService.createTask(taskEntity);
//        nettyFileSyncService.syncBlogDataFirstStep();
    }

    /**
     * 文件同步任务-设备端文件上传任务
     * 服务器端：
     * 1. 发送netty文件同步消息
     * 设备端：
     * 1. 收到netty文件同步请求，回复消息响应
     * 2. 通知socket将需要上传的文件移动到docker容器共享目录中
     * 3. socket响应完成，使用ftp上传文件
     * 4. ftp文件上传成功，netty响应文件同步完成
     * 服务器端：
     * 1. 服务器收到文件上传成功消息，调用minio服务，将目录下文件导入minio
     * 2. 删除临时文件
     */
    public void deviceFileUploadTask() {



        logger.info("定时任务 执行：文件同步任务-设备端文件上传任务");
        nettyFileSyncService.syncDeviceFile();
    }
}
