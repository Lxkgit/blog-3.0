package com.blog.gateway.mq;

import com.blog.mq.entity.MqMessage;
import com.blog.mq.listener.MqMessageHandler;
import com.blog.mq.service.MQProducerService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Description 收到的mq消息
 * @Author lxk
 * @CreateTime 2025-01-05
 */

@Service
public class MqMessageHandlerService implements MqMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MqMessageHandlerService.class);

    @Resource
    private MQProducerService mqProducerService;

    @Override
    public boolean handleMessage(MqMessage rocketMqMessage) {

        return false;
    }
}
