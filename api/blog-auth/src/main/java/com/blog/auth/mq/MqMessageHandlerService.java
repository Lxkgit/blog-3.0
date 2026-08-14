package com.blog.auth.mq;

import com.blog.mq.entity.MqMessage;
import com.blog.mq.listener.MqMessageHandler;
import com.blog.mq.service.MQProducerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Description 收到的mq消息
 * @Author lxk
 * @CreateTime 2025-01-05
 */

@Service
public class MqMessageHandlerService implements MqMessageHandler {

    @Resource
    private MQProducerService mqProducerService;

    @Override
    public boolean handleMessage(MqMessage rocketMqMessage) {

        return false;
    }
}
