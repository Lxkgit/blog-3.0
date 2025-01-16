package com.blog.mq.listener;

import com.alibaba.fastjson.JSON;
import com.blog.mq.entity.RocketMQMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;

/**
 * RocketMQConsumeMsgListenerProcessor
 */
@Component
@Slf4j
public class RocketMQConsumerMsgListenerProcessor implements MessageListenerConcurrently {

    @Resource
    RocketMQMessageHandler rockerMQMessageHandler;

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgList, ConsumeConcurrentlyContext context) {
        if (CollectionUtils.isEmpty(msgList)) {
            log.info("接受到的消息为空，不处理，直接返回成功");
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        MessageExt messageExt = msgList.get(0);
        int reconsume = messageExt.getReconsumeTimes();
        if (reconsume == 3) {//消息已经重试了3次，如果不需要再次消费，则返回成功
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        String msgBody = new String(messageExt.getBody(), Charset.forName(RemotingHelper.DEFAULT_CHARSET));
        RocketMQMessage rocketMQMessage = JSON.parseObject(msgBody, RocketMQMessage.class);
        String topic = rocketMQMessage.getTopic();
        String tag = rocketMQMessage.getTag();
        log.info("RocketMQ receive message: " + rocketMQMessage.toString());

        rockerMQMessageHandler.handleMessage(topic, tag, rocketMQMessage);

        // 如果没有return success ，consumer会重新消费该消息，直到return success
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

}