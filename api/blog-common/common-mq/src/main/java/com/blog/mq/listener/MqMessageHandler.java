package com.blog.mq.listener;

import com.blog.mq.entity.MqMessage;

/**
 * @author lxk
 * @description mq消息处理接口类
 * @date 2025/01/05
 */

public interface MqMessageHandler {

    boolean handleMessage(MqMessage rocketMqMessage);
}
