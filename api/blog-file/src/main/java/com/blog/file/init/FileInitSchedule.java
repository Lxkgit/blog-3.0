package com.blog.file.init;

import com.alibaba.fastjson2.JSON;
import com.blog.core.domain.file.system.entity.BlogData;
import com.blog.core.enums.mq.RocketMQMsgEnum;
import com.blog.core.enums.mq.RocketMQTopicEnum;
import com.blog.file.mapper.FileCategoryDataMapper;
import com.blog.mq.entity.RocketMQMessage;
import com.blog.mq.service.MQProducerService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @description:
 * @Author: lxk
 * @date 2023/7/11 20:06
 */

@Slf4j
@Configuration     //证明这个类是一个配置文件
@EnableScheduling  //启用定时器
public class FileInitSchedule {

//    @Resource
//    private FileCategoryDataMapper fileDataMapper;
//
//    @Resource
//    private MQProducerService mqProducerService;
//
//
//    @PostConstruct
//    @Scheduled(cron = "0 0 0 * * ?")
//    public void initFile() {
//        log.info("开始初始化博客文件数据 ... ");
//        BlogData blogData = new BlogData();
//        blogData.setImgCount(fileDataMapper.selectImgCount());
//        RocketMQMessage rocketMQMessage = new RocketMQMessage();
//        rocketMQMessage.setTopic(RocketMQTopicEnum.BLOG_SYSTEM_DATA.getTopic());
//        rocketMQMessage.setTag(RocketMQTopicEnum.BLOG_SYSTEM_DATA.getTag());
//        rocketMQMessage.setMessage(JSON.toJSONString(blogData));
//        rocketMQMessage.setMqMsgType(RocketMQMsgEnum.ALL.getType());
//        SendResult sendResult = mqProducerService.sendSyncOrderly(rocketMQMessage);
//        log.info("sendMQResult:{}", sendResult);
//    }
}
