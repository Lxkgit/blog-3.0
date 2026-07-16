package com.blog.file.task;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.blog.core.constant.Constant;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.netty.head.TaskMsgHead;
import com.blog.core.domain.file.task.del.bo.SyncDeviceFileBo;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.timer.context.TimerTaskContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class DeviceFileUploadTaskAction extends SystemTimerAction {

    @Resource
    private NettySyncFileService nettySyncFileService;

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
        SyncDeviceFileBo bo = context.get("");
        nettySyncFileService.syncDeviceFile(bo, new MsgHead());
        return "";
    }

    @Override
    public String getParamTemplate() {
        JSONArray array = new JSONArray();

        JSONObject servicePath = new JSONObject();
        servicePath.put("type", "input");
        servicePath.put("name", "服务器文件路径");
        servicePath.put("paramName", "servicePath");
        servicePath.put("length", "200");
        array.add(servicePath);

        JSONObject devicePath = new JSONObject();
        devicePath.put("type", "input");
        devicePath.put("name", "设备文件路径");
        devicePath.put("paramName", "devicePath");
        devicePath.put("length", "200");
        array.add(devicePath);

        JSONObject syncCount = new JSONObject();
        syncCount.put("type", "input-number");
        syncCount.put("name", "同步文件数量");
        syncCount.put("paramName", "syncCount");
        syncCount.put("min", 0);
        syncCount.put("max", 50);
        array.add(syncCount);

        JSONObject maxFileCount = new JSONObject();
        maxFileCount.put("type", "input-number");
        maxFileCount.put("name", "目录下最大文件数量");
        maxFileCount.put("paramName", "maxFileCount");
        maxFileCount.put("min", 0);
        maxFileCount.put("max", 200);
        array.add(maxFileCount);

        return array.toString();
    }

}
