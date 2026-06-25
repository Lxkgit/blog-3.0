package com.blog.file.task;

import com.blog.core.constant.Constant;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.file.task.bo.SyncServiceFileBo;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @Description minio文件数据同步
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class MinioFileSyncTaskAction implements TimerAction {


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
    public void execute(TimerTaskContext context) {
        SyncServiceFileBo bo = context.get("");
        nettySyncFileService.syncServiceFile(bo, new MsgHead());
    }
}
