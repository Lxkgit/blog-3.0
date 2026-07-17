package com.blog.file.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.blog.core.constant.Constant;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.netty.head.TaskMsgHead;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.file.socket.service.SocketMessageSendService;
import com.blog.timer.context.TimerTaskContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class ClearTempFileOrPathAction extends SystemTimerAction {

    private static final Logger logger = LoggerFactory.getLogger(BlogDateSyncTaskAction.class);

    @Resource
    private SocketMessageSendService socketMessageSendService;


    @Override
    public String doExecute(TimerTaskContext context, TaskMsgHead taskMsgHead) {
        String clearPath = clearTempFileOrPath(context.get("deleteFilePath"), new MsgHead());
        return JSON.toJSONString(Collections.singletonMap("deleteFilePath", clearPath));
    }

    @Override
    public String getCode() {
        return Constant.TASK_DELETE_TEMP_FILE;
    }

    @Override
    public String getName() {
        return "清理服务器文件";
    }

    @Override
    public String getParamTemplate() {
        JSONArray array = new JSONArray();

        JSONObject deviceFilePath = new JSONObject();
        deviceFilePath.put("type", "input");
        deviceFilePath.put("name", "清理文件路径");
        deviceFilePath.put("paramName", "deleteFilePath");
        deviceFilePath.put("length", "200");
        array.add(deviceFilePath);

        return array.toString();
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
}
