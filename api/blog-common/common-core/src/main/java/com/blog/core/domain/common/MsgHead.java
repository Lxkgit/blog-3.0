package com.blog.core.domain.common;

import lombok.Data;

import java.util.UUID;

/**
 * @Description 消息头
 * @Author lxk
 * @CreateTime 2025-09-19
 */

@Data
public class MsgHead {

    /**
     * 消息类型
     * 1: 系统通信触发
     * 2: 定时任务触发
     * 3: 手动触发
     */
    private Integer msgType;

    /**
     * 消息所属用户id
     */
    private Integer userId;

    /**
     * 定时任务触发消息头
     */
    private TaskMsgHead taskMsgHead;

    /**
     * netty 消息头
     */
    private NettyMsgHead nettyMsgHead;

    public static MsgHead buildTaskMsgHead(Integer userId, TaskMsgHead taskMsgHead) {
        MsgHead msgHead = new MsgHead();
        msgHead.setMsgType(1);
        msgHead.setUserId(userId);
        msgHead.setTaskMsgHead(taskMsgHead);
        return msgHead;
    }
}
