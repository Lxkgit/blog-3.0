package com.blog.content.mq.send;

import com.alibaba.fastjson.JSON;

import com.blog.core.domain.file.system.vo.BlogDataVo;
import com.blog.mq.enums.MqMsgEnum;
import com.blog.mq.enums.MqTopicEnum;
import com.blog.mq.entity.MqMessage;
import com.blog.mq.service.MQProducerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


/**
 * @description:
 * @Author: lxk
 * @date 2023/12/27 14:50
 */

@Component
public class SendSystemData {

    // 发送文章
    public static final Integer article = 1;
    // 发送文章分类
    public static final Integer articleType = 2;
    // 发送文章标签
    public static final Integer articleLabel = 3;
    // 发送文档
    public static final Integer doc = 4;
    // 发送文档分类
    public static final Integer docType = 5;
    // 发送日记
    public static final Integer diary = 6;

    @Resource
    private MQProducerService mqProducerService;

    /**
     * 消息中组装单一种类数据
     *
     * @param type  1: 发送文章 2: 发送文章分类 3: 发送文章标签 4: 发送文档 5: 发送文档分类 6: 发送日记
     * @param count 数量
     */
    public void sendSystemData(Integer type, Integer count) {
        BlogDataVo blogDataVo = new BlogDataVo();

        if (type.equals(article)) {
            blogDataVo.setArticleCount(count);
        } else if (type.equals(articleType)) {
            blogDataVo.setArticleTypeCount(count);
        } else if (type.equals(articleLabel)) {
            blogDataVo.setArticleLabelCount(count);
        } else if (type.equals(doc)) {
            blogDataVo.setDocCount(count);
        } else if (type.equals(docType)) {
            blogDataVo.setDocTypeCount(count);
        } else if (type.equals(diary)) {
            blogDataVo.setDiaryCount(count);
        } else {
            return;
        }

        MqMessage mqMessage = new MqMessage(MqTopicEnum.BLOG_SYSTEM_DATA,
                MqMsgEnum.ADD.getType(), JSON.toJSONString("blogDataVo"));
        mqProducerService.sendSyncOrderly(mqMessage);
    }
}
