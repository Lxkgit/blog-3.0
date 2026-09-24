package com.blog.mq.enums;

import com.blog.mq.constant.MqTopicConstant;

/**
 * @description: mq topic基础类
 * @Author: lxk
 * @date 2023/6/28 16:50
 */

public enum MqTopicEnum {

    // 博客数据类数据统计
    BLOG_USER_DATA(MqTopicConstant.BLOG_USER_DATA, MqTopicConstant.CONTENT , "博客用户内容统计数据"),
    BLOG_SYSTEM_DATA(MqTopicConstant.BLOG_SYSTEM_DATA, MqTopicConstant.CONTENT, "博客系统内容消息"),
    BLOG_SYSTEM_DATA_REGISTER(MqTopicConstant.BLOG_SYSTEM_DATA, MqTopicConstant.REGISTER, "用户注册消息"),

    BLOG_SYSTEM_USER_IP(MqTopicConstant.BLOG_SYSTEM_DATA, MqTopicConstant.USER_IP, "博客系统用户请求ip"),
    ;

    /**
     * topic
     */
    private String topic;

    /**
     * tag
     */
    private String tag;

    /**
     * 备注
     */
    private String memo;

    MqTopicEnum(String topic, String tag, String memo) {
        this.topic = topic;
        this.tag = tag;
        this.memo = memo;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
