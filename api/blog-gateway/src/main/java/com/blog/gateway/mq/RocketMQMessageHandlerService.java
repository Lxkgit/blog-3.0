package com.blog.gateway.mq;

import com.blog.mq.entity.RocketMQMessage;
import com.blog.mq.listener.RocketMQMessageHandler;
import com.blog.mq.service.MQProducerService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Description 收到的mq消息
 * @Author lxk
 * @CreateTime 2025-01-05
 */

@Service
public class RocketMQMessageHandlerService implements RocketMQMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(RocketMQMessageHandlerService.class);

    @Resource
    private MQProducerService mqProducerService;

    @Override
    public boolean handleMessage(String topic, String tag, RocketMQMessage rocketMQMessage) {

        return false;
    }
}
