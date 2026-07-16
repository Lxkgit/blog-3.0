package com.blog.file.task;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.blog.core.constant.Constant;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.netty.head.TaskMsgHead;
import com.blog.core.domain.file.task.del.bo.SyncServiceFileBo;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.timer.context.TimerTaskContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @Description minio文件数据同步
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class MinioFileSyncTaskAction extends SystemTimerAction {


    @Resource
    private NettySyncFileService nettySyncFileService;

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
        SyncServiceFileBo bo = context.get("");
        nettySyncFileService.syncServiceFile(bo, MsgHead.buildTaskMsgHead(taskMsgHead.getUserId(), taskMsgHead));
        return "";
    }

    @Override
    public String getParamTemplate() {
        // [{ "userId": 1, "dirPath": "/",
        // "clearPath":["/1/user/data/img/1", "/1/user/data/img/2", "/1/user/data/video/1", "/1/user/data/video/2",
        // "/1/user/data/video/3", "/1/user/data/video/4", "/1/user/data/video/5"] }]

        JSONArray array = new JSONArray();

        JSONObject minioPath = new JSONObject();
        minioPath.put("type", "input");
        minioPath.put("name", "服务器文件路径");
        minioPath.put("paramName", "servicePath");
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
        count.put("paramName", "syncCount");
        count.put("min", 0);
        count.put("max", 50);
        array.add(count);

        JSONObject maxFileCount = new JSONObject();
        maxFileCount.put("type", "input-number");
        maxFileCount.put("name", "目录下最大文件数量");
        maxFileCount.put("paramName", "maxCount");
        maxFileCount.put("min", 0);
        maxFileCount.put("max", 200);
        array.add(maxFileCount);

        return array.toString();
    }
}
