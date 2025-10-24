package com.blog.file.task;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.task.service.CreateTaskService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-10-14
 */

@Service
public class TaskLogService {

    @Resource
    private CreateTaskService createTaskService;

    public void recordTaskLog(NettySyncFileDto nettyFileSyncDto, MsgHead msgHead) {
        TaskLog taskLog = new TaskLog();
        if (msgHead != null && msgHead.getTaskMsgHead() != null) {
            taskLog.setTaskCode(msgHead.getTaskMsgHead().getTaskCode());
            taskLog.setChildTaskCode(msgHead.getTaskMsgHead().getChildTaskCode());
            taskLog.setTaskUUID(msgHead.getTaskMsgHead().getTaskUUID());
        }
        if (nettyFileSyncDto != null) {
            taskLog.setTaskResultStatus(nettyFileSyncDto.getSyncResult());
            taskLog.setTaskResult(JSONObject.toJSONString(nettyFileSyncDto));
        }
        if (StringUtils.isNotEmpty(taskLog.getTaskUUID())) {
            taskLog.setTaskLogType(2);
            createTaskService.recordTaskLog(taskLog);
        }
    }

}
