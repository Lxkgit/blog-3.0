package com.blog.mq.listener;

import com.blog.mq.entity.RocketMQMessage;

/**
 * @author lxk
 * @description mq消息处理接口类
 * @date 2025/01/05
 */

public interface RocketMQMessageHandler {

    boolean handleMessage(String topic, String tag, RocketMQMessage rocketMQMessage);
}
