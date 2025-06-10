package com.blog.file.xxlJob;

import com.blog.core.constant.Constant;
import com.blog.core.utils.MyStringUtils;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.service.NettyFileSyncService;
import com.blog.file.socket.SocketService;
import com.blog.file.socket.domain.SocketPacket;
import com.blog.file.socket.domain.constant.SocketClientType;
import com.blog.file.socket.domain.constant.SocketConstant;
import com.blog.file.socket.domain.constant.SocketTopic;
import com.blog.file.socket.domain.dto.ExportBlogFileDto;
import com.blog.file.socket.domain.dto.MoveFileDto;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 */
@Component
public class XxlJobTask {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobTask.class);

    @Resource
    private SocketService socketService;

    @Resource
    private NettyFileSyncService nettyFileSyncService;

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
        logger.info("执行 demoJobHandler 定时任务");
        System.out.println("XXL-JOB, Hello World.");
        XxlJobHelper.log("XXL-JOB, Hello World.");

        for (int i = 0; i < 5; i++) {
            XxlJobHelper.log("beat at:" + i);
            TimeUnit.SECONDS.sleep(2);
        }
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
    @XxlJob("blogDateSyncTask")
    public void blogDateSyncTask() {
        logger.info("xxlJob 执行：文件同步任务-定时备份博客数据");

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
    @XxlJob("deviceFileUploadTask")
    public void deviceFileUploadTask() {
        logger.info("xxlJob 执行：文件同步任务-设备端文件上传任务");

        MoveFileDto moveFileDto = new MoveFileDto();
        SocketPacket<MoveFileDto> requestPacket = SocketPacket.buildRequest(SocketTopic.SOCKET_MOVE_FILE, moveFileDto);
        socketService.sendMessage(SocketClientType.PYTHON, SocketConstant.LOCALHOST_REGISTER_CODE, requestPacket);

    }



}

