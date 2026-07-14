package com.blog.file.task;

import com.alibaba.excel.metadata.Head;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.blog.core.constant.Constant;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.common.TaskMsgHead;
import com.blog.core.utils.MyStringUtils;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.timer.context.TimerTaskContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Description 定时备份博客数据
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class BlogDateSyncTaskAction extends SystemTimerAction {

    private static final Logger logger = LoggerFactory.getLogger(BlogDateSyncTaskAction.class);

    @Resource
    private NettySyncFileService nettySyncFileService;

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
        logger.info("开始备份博客数据 : {}", JSONObject.toJSONString(context.getData()));
        String blogFilePath = Constant.FTP_PATH_SYSTEM_TEMP + "/" + MyStringUtils.getRandomString(6);
        taskMsgHead.setTaskParam(context.get("param"));
        String filePath = nettySyncFileService.syncBlogDataFirstStep(blogFilePath, MsgHead.buildTaskMsgHead(taskMsgHead.getUserId(), taskMsgHead));
        JSONObject jsonObject = JSONObject.parseObject(filePath);
        return jsonObject.toJSONString();
    }

    @Override
    public String getCode() {
        return Constant.TASK_SYNC_BLOG_FILE;
    }

    @Override
    public String getName() {
        return "定时备份博客数据";
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
}
