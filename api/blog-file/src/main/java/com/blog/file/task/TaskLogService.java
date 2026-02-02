package com.blog.file.task;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.socket.domain.dto.SocketDeleteFileOrDirDto;
import com.blog.task.service.impl.CreateTaskService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-10-14
 */

@Service
public class TaskLogService {

    @Resource
    private CreateTaskService createTaskService;

    private TaskLog setTaskLogHead(MsgHead msgHead) {
        TaskLog taskLog = new TaskLog();
        taskLog.setStartTime(new Date());
        if (msgHead != null && msgHead.getTaskMsgHead() != null) {
            taskLog.setTaskCode(msgHead.getTaskMsgHead().getTaskCode());
            taskLog.setChildTaskCode(msgHead.getTaskMsgHead().getChildTaskCode());
            taskLog.setTaskUUID(msgHead.getTaskMsgHead().getTaskUUID());
        }
        return taskLog;
    }

    /**
     * 记录文件同步日志
     *
     * @param nettyFileSyncDto
     * @param msgHead
     */
    public void recordSyncFileTaskLog(NettySyncFileDto nettyFileSyncDto, MsgHead msgHead) {
        TaskLog taskLog = setTaskLogHead(msgHead);
        if (nettyFileSyncDto != null) {
            taskLog.setTaskResultStatus(nettyFileSyncDto.getSyncResult());
            taskLog.setTaskResult(JSONObject.toJSONString(nettyFileSyncDto));
        }
        if (StringUtils.isNotEmpty(taskLog.getTaskUUID())) {
            taskLog.setTaskLogType(2);
            createTaskService.recordTaskLog(taskLog);
        }
    }


    /**
     * 记录文件删除日志
     *
     * @param dto
     * @param msgHead
     */
    public void recordDelFileTaskLog(SocketDeleteFileOrDirDto dto, MsgHead msgHead) {
        TaskLog taskLog = setTaskLogHead(msgHead);
        if (dto != null) {
            taskLog.setTaskResultStatus(dto.getResult() ? 1 : 0);
            taskLog.setTaskResult(JSONObject.toJSONString(dto));
        }
        if (StringUtils.isNotEmpty(taskLog.getTaskUUID())) {
            taskLog.setTaskLogType(2);
            createTaskService.recordTaskLog(taskLog);
        }
    }

}
