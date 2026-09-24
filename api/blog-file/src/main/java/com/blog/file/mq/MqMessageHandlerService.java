package com.blog.file.mq;

import com.blog.file.mq.service.BlogSystemService;
import com.blog.file.mq.service.UserRegisterService;
import com.blog.mq.constant.MqTopicConstant;
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

    @Resource
    private UserRegisterService userRegisterService;

    @Resource
    private BlogSystemService blogSystemService;

    @Override
    public boolean handleMessage(MqMessage mqMessage) {
        String topic = mqMessage.getTopic();
        String tag = mqMessage.getTag();
        if (MqTopicConstant.BLOG_USER_DATA.equals(topic)) {
            if (MqTopicConstant.CONTENT.equals(tag)) {

            }
        } else if (MqTopicConstant.BLOG_SYSTEM_DATA.equals(topic)) {
            if (MqTopicConstant.REGISTER.equals(tag)) {
                logger.info("===== 用户注册 =====");
                userRegisterService.createDir(mqMessage);
            }
            if (MqTopicConstant.USER_IP.equals(tag)) {
                logger.info("===== IP定位 =====");

                blogSystemService.getIpLocation(mqMessage);
            }
        }

        return false;
    }
}
