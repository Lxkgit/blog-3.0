package com.blog.mq.listener;

import com.alibaba.fastjson.JSON;
import com.blog.mq.entity.MqMessage;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @Description mq消息处理
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class MqConsumerMsgListenerProcessor implements MessageListenerConcurrently {

    private static final Logger logger = LoggerFactory.getLogger(MqConsumerMsgListenerProcessor.class);

    @Resource
    private MqMessageHandler mqMessageHandler;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgList, ConsumeConcurrentlyContext context) {
        if (CollectionUtils.isEmpty(msgList)) {
            logger.info("接受到的消息为空，不处理，直接返回成功");
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        MessageExt messageExt = msgList.get(0);
        int reconsume = messageExt.getReconsumeTimes();
        if (reconsume == 3) {
            //消息已经重试了3次，如果不需要再次消费，则返回成功
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        String msgBody = new String(messageExt.getBody(), Charset.forName(RemotingHelper.DEFAULT_CHARSET));
        MqMessage mqMessage = JSON.parseObject(msgBody, MqMessage.class);

        logger.info("RocketMQ receive message: {}", mqMessage);

        mqMessageHandler.handleMessage(mqMessage);

        // 如果没有return success ，consumer会重新消费该消息，直到return success
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

}