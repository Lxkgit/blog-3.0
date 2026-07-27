package com.blog.file.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.blog.core.constant.Constant;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.netty.dto.file.NettySyncFileDto;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.netty.head.TaskMsgHead;
import com.blog.core.domain.socket.SocketPacket;
import com.blog.core.domain.socket.constant.SocketClientType;
import com.blog.core.domain.socket.constant.SocketConstant;
import com.blog.core.domain.socket.constant.SocketTopic;
import com.blog.core.domain.socket.dto.SocketExportBlogFileDto;
import com.blog.core.utils.MyStringUtils;
import com.blog.file.service.TaskLogService;
import com.blog.file.socket.config.SocketService;
import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @Description 定时备份博客数据
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class BlogDateSyncTaskAction extends SystemTimerAction {

    private static final Logger logger = LoggerFactory.getLogger(BlogDateSyncTaskAction.class);

    @Resource
    private SocketService socketService;

    @Resource
    private TaskLogService taskLogService;

    @Override
    public String getCode() {
        return Constant.TASK_SYNC_BLOG_FILE;
    }

    @Override
    public String getName() {
        return "定时备份博客数据";
    }

    @Override
    protected void doCheckParam(TimerAction action, String param) {
        JSONObject paramJson = JSON.parseObject(param);

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
    @Override
    public String doExecute(TimerTaskContext context, TaskMsgHead taskMsgHead) {
        logger.info("开始备份博客数据: {}", JSONObject.toJSONString(context.getData()));
        String blogFilePath = Constant.FTP_PATH_SYSTEM_TEMP + "/" + MyStringUtils.getRandomString(6);
        taskMsgHead.setTaskParam(context.get("param"));
        String filePath = syncBlogDataFirstStep(blogFilePath, MsgHead.buildTaskMsgHead(taskMsgHead.getUserId(), taskMsgHead));
        return JSON.toJSONString(Collections.singletonMap("tempFilePath", filePath));
    }

    @Override
    public String getParamTemplate() {
        JSONArray array = new JSONArray();

        JSONObject deviceFilePath = new JSONObject();
        deviceFilePath.put("type", "input");
        deviceFilePath.put("name", "备份文件存放路径");
        deviceFilePath.put("paramName", "deviceFilePath");
        deviceFilePath.put("length", "200");
        array.add(deviceFilePath);

        return array.toString();
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
        JSONObject param = JSONObject.parseObject(msgHead.getTaskMsgHead().getTaskParam());
        String deviceFilePath = param.getString("deviceFilePath");

        // 构建netty发送消息包
        NettySyncFileDto nettySyncFileDto = NettySyncFileDto.buildSyncToDevice(serviceFilePath, deviceFilePath);
        nettySyncFileDto.setFileNameList(List.of(fileName));

        taskLogService.taskStartLog(log.getId());

        // 发送netty消息开始下载博客文件
        boolean result = nettySyncFileSendService.sendSyncFileMsg(msgHead, nettySyncFileDto, msgHead.getUserId());

        if (result) {
            taskLogService.taskSuccessLog(log.getId(), JSONObject.parseObject(deviceFilePath).toJSONString());
        } else {
            taskLogService.taskFailureLog(log.getId(), null);
        }
    }
}
