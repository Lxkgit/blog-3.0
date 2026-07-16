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
        JSONArray array = new JSONArray();

        JSONObject minioPath = new JSONObject();
        minioPath.put("type", "input");
        minioPath.put("name", "minio同步目录");
        minioPath.put("paramName", "minioPath");
        minioPath.put("length", "200");
        array.add(minioPath);

        return array.toString();
    }
}
