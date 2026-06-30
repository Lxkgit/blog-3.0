package com.blog.file.task;

import com.blog.core.constant.Constant;
import com.blog.core.domain.common.MsgHead;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class ClearTempFileOrPathAction implements TimerAction {

    private static final Logger logger = LoggerFactory.getLogger(BlogDateSyncTaskAction.class);

    @Resource
    private NettySyncFileService nettySyncFileService;

    @Override
    public void execute(TimerTaskContext context) {
        nettySyncFileService.clearTempFileOrPath(context.get("path"), new MsgHead());
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
        return "";


    }

    @Override
    public void checkParam(String json) {

    }
}
