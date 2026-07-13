package com.blog.file.task;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.blog.core.constant.Constant;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.common.TaskMsgHead;
import com.blog.core.domain.file.task.del.bo.SyncDeviceFileBo;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTask;
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

        JSONObject minioPath = new JSONObject();
        minioPath.put("type", "input");
        minioPath.put("name", "服务器文件路径");
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
