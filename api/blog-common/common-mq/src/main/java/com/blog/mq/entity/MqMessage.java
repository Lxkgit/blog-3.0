package com.blog.mq.entity;

import com.blog.mq.enums.MqTopicEnum;
import lombok.Data;

/**
 * @description:
 * @Author: lxk
 * @date 2023/7/5 14:12
 */

@Data
public class MqMessage {

    /**
     * mq topic
     */
    private String topic;

    /**
     * mq tag
     */
    private String tag;

    /**
     * mq消息类型 0：全量 1：增量
     */
    private Integer mqMsgType;

    /**
     * 消息体
     */
    private String message;

    /**
     * 消息类
     */
    private Class<?> messageClass;

    public MqMessage() {
    }

    public MqMessage(MqTopicEnum mqTopicEnum) {
        this.topic = mqTopicEnum.getTopic();
        this.tag = mqTopicEnum.getTag();
    }

    public MqMessage(MqTopicEnum mqTopicEnum, Integer mqMsgType, String message) {
        this.topic = mqTopicEnum.getTopic();
        this.tag = mqTopicEnum.getTag();
        this.mqMsgType = mqMsgType;
        this.message = message;
    }

    public MqMessage(MqTopicEnum mqTopicEnum, Integer mqMsgType, String message, Class<?> messageClass) {
        this.topic = mqTopicEnum.getTopic();
        this.tag = mqTopicEnum.getTag();
        this.mqMsgType = mqMsgType;
        this.message = message;
        this.messageClass = messageClass;
    }


}
