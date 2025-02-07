package com.blog.file.mq;

import com.blog.mq.entity.RocketMQMessage;
import com.blog.mq.listener.RocketMQMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description 收到的mq消息
 * @Author lxk
 * @CreateTime 2025-01-05
 */

@Slf4j
@Service
public class RocketMQMessageHandlerService implements RocketMQMessageHandler {


    @Override
    public boolean handleMessage(String topic, String tag, RocketMQMessage rocketMQMessage) {
        log.info(rocketMQMessage.toString());
        return false;
    }
}
