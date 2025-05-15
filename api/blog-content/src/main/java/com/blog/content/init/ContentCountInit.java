package com.blog.content.init;

import com.alibaba.fastjson2.JSON;
import com.blog.content.mapper.mybatis.ArticleLabelMapper;
import com.blog.content.mapper.mybatis.ArticleMapper;
import com.blog.content.mapper.mybatis.DiaryMapper;
import com.blog.content.mapper.mybatis.DocContentMapper;
import com.blog.core.domain.file.system.entity.BlogData;
import com.blog.core.domain.file.system.entity.ContentCount;
import com.blog.core.enums.mq.RocketMQMsgEnum;
import com.blog.core.enums.mq.RocketMQTopicEnum;
import com.blog.mq.entity.RocketMQMessage;
import com.blog.mq.service.MQProducerService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description: 博客内容统计初始化
 * @Author: lxk
 * @date 2023/6/29 11:06
 */

@Slf4j
@Configuration     //证明这个类是一个配置文件
@EnableScheduling  //启用定时器
public class ContentCountInit {

    // 发送文章
    private static final Integer article = 1;
    // 发送日记
    private static final Integer diary = 2;
    // 发送文档
    private static final Integer doc = 3;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private DiaryMapper diaryMapper;

    @Resource
    private DocContentMapper docContentMapper;

    @Resource
    private ArticleLabelMapper articleLabelMapper;

    @Resource
    private MQProducerService mqProducerService;

    @PostConstruct
    @Scheduled(cron = "0 0 0 * * ?")
    public void initContent() {
        log.info("开始初始化博客内容数据 ... ");

        // 初始化系统消息数据
//        initSystemData();
//
//        initUserData();

    }

    /**
     * 初始化系统消息mq
     */
    private void initSystemData() {
        BlogData blogData = new BlogData();
        blogData.setArticleCount(articleMapper.selectArticleCount());
        blogData.setArticleLabelCount(articleLabelMapper.selectArticleLabelCount());
        blogData.setDocCount(docContentMapper.selectDocContentCount());
        blogData.setArticleTypeCount(0);
        blogData.setDocTypeCount(0);
        blogData.setDiaryCount(diaryMapper.selectDiaryCount());
        RocketMQMessage rocketMQMessage = new RocketMQMessage();
        rocketMQMessage.setTopic(RocketMQTopicEnum.BLOG_SYSTEM_DATA.getTopic());
        rocketMQMessage.setTag(RocketMQTopicEnum.BLOG_SYSTEM_DATA.getTag());
        rocketMQMessage.setMessage(JSON.toJSONString(blogData));
        rocketMQMessage.setMqMsgType(RocketMQMsgEnum.ALL.getType());
        mqProducerService.sendSyncOrderly(rocketMQMessage);
    }

    /**
     * 初始化用户消息
     */
    private void initUserData() {
        List<ContentCount> contentCountList = new ArrayList<>();

//        List<Map<String,Integer>> articleList = articleMapper.selectArticleCountGroupByUserId();
//        setContentCountList(contentCountList, articleList, article);
//        List<Map<String,Integer>> diaryList = diaryMapper.selectDiaryCountGroupByUserId();
//        setContentCountList(contentCountList, diaryList, diary);
//        List<Map<String,Integer>> docList = docContentMapper.selectDocCountGroupByUserId();
//        setContentCountList(contentCountList, docList, doc);

        RocketMQMessage rocketMQMessage = new RocketMQMessage();
        rocketMQMessage.setTopic(RocketMQTopicEnum.BLOG_USER_DATA.getTopic());
        rocketMQMessage.setTag(RocketMQTopicEnum.BLOG_USER_DATA.getTag());
        rocketMQMessage.setMessage(JSON.toJSONString(contentCountList));
        rocketMQMessage.setMqMsgType(RocketMQMsgEnum.ALL.getType());
        mqProducerService.sendSyncOrderly(rocketMQMessage);
    }

    /**
     * 组装mq消息数据
     * @param contentCountList
     * @param mapList
     * @param type
     */
    private void setContentCountList(List<ContentCount> contentCountList, List<Map<String, Integer>> mapList, Integer type) {
        mapList.forEach(item -> {
            int userId = Integer.parseInt(String.valueOf(item.get("userId")));
            AtomicBoolean flag = new AtomicBoolean(false);
            contentCountList.forEach(contentCount -> {
                if (contentCount.getUserId().equals(userId)) {
                    flag.set(true);
                    setContentCount(type, item, contentCount);
                }
            });
            if (!flag.get()) {
                ContentCount contentCount = new ContentCount();
                contentCount.setUserId(userId);
                setContentCount(type, item, contentCount);
                contentCountList.add(contentCount);
            }
        });
    }

    private void setContentCount(Integer type, Map<String, Integer> item, ContentCount contentCount) {
        if (type.equals(article)) {
            contentCount.setArticleCount(Integer.parseInt(String.valueOf(item.get("count"))));
        } else if (type.equals(diary)) {
            contentCount.setDiaryCount(Integer.parseInt(String.valueOf(item.get("count"))));
        } else if (type.equals(doc)) {
            contentCount.setDocCount(Integer.parseInt(String.valueOf(item.get("count"))));
        }
    }
}
