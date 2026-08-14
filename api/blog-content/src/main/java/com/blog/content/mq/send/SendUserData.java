package com.blog.content.mq.send;

import com.alibaba.fastjson.JSON;
import com.blog.core.domain.file.system.vo.ContentCountVo;
import com.blog.mq.enums.MqMsgEnum;
import com.blog.mq.enums.MqTopicEnum;
import com.blog.mq.entity.MqMessage;
import com.blog.mq.service.MQProducerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


/**
 * @description: MQ消息快速发送类 BLOG_USER_DATA("BLOG_USER_DATA", "CONTENT" , "博客用户内容统计数据"),
 * @Author: lxk
 * @date 2023/12/27 14:31
 */

@Component
public class SendUserData {

    // 发送文章
    public static final Integer ARTICLE = 1;
    // 发送日记
    public static final Integer DIARY = 2;
    // 发送文档
    public static final Integer DOC = 3;

    @Resource
    private MQProducerService mqProducerService;

    /**
     * 消息中组装单一种类数据
     *
     * @param type   1: 发送文章 2: 发送日记 3: 发送文档
     * @param userId 用户id
     * @param count  数量
     */
    public void sendUserData(Integer type, Integer userId, Integer count) {
        ContentCountVo contentCountVo = new ContentCountVo();
        contentCountVo.setUserId(userId);
        if (type.equals(ARTICLE)) {
            contentCountVo.setArticleCount(count);
        } else if (type.equals(DIARY)) {
            contentCountVo.setDiaryCount(count);
        } else if (type.equals(DOC)) {
            contentCountVo.setDocCount(count);
        } else {
            return;
        }
        MqMessage mqMessage = new MqMessage(MqTopicEnum.BLOG_USER_DATA, MqMsgEnum.ADD.getType(), JSON.toJSONString("contentCountVo"));
        mqProducerService.sendSyncOrderly(mqMessage);
    }
}
