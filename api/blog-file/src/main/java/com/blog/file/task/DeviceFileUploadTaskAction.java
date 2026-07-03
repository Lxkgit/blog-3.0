package com.blog.file.task;

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
        return "";
    }

}
