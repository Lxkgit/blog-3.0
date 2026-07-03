package com.blog.file.task;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.constant.Constant;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.common.TaskMsgHead;
import com.blog.file.netty.service.NettySyncFileService;
import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTask;
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
public class ClearTempFileOrPathAction extends SystemTimerAction {

    private static final Logger logger = LoggerFactory.getLogger(BlogDateSyncTaskAction.class);

    @Resource
    private NettySyncFileService nettySyncFileService;

    @Override
    public String doExecute(TimerTaskContext context, TaskMsgHead taskMsgHead) {
        String clearPath = nettySyncFileService.clearTempFileOrPath(context.get("path"), new MsgHead());
        JSONObject jsonObject = JSONObject.parseObject(clearPath);
        return jsonObject.toJSONString();
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
}
