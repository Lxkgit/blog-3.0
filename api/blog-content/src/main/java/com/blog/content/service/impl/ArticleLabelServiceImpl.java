package com.blog.content.service.impl;


import com.blog.content.mapper.mybatis.ArticleLabelMapper;
import com.blog.content.mapper.mybatis.ArticleLabelTypeMapper;
import com.blog.content.mq.send.SendSystemData;
import com.blog.content.service.ArticleLabelService;
import com.blog.core.constant.ErrorConstant;
import com.blog.core.domain.content.article.entity.ArticleLabel;
import com.blog.core.domain.content.article.vo.ArticleLabelVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author: lxk
 * @date: 2022/6/15 18:56
 * @description: 文章标签服务类
 * @modified By:
 */

@Service
public class ArticleLabelServiceImpl implements ArticleLabelService {

    @Resource
    private ArticleLabelMapper articleLabelMapper;

    @Resource
    private ArticleLabelTypeMapper articleLabelTypeMapper;

    @Resource
    private SendSystemData sendSystemData;

    /**
     * 新增文章标签
     *
     * @param articleLabelVo
     * @throws ServiceException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveArticleLabel(ArticleLabelVo articleLabelVo) throws ServiceException {
        articleLabelVo.setId(null);
        articleLabelVo.setUserId(SecurityUtil.getLoginUser().getId());
        articleLabelVo.setArticleNum(0);
        articleLabelVo.setCreateTime(new Date());
        articleLabelVo.setUpdateTime(new Date());
        if (articleLabelTypeMapper.selectById(articleLabelVo.getLabelType()) == null) {
            throw new ServiceException(ErrorConstant.ARTICLE_LABEL_TYPE_NOT_EXISTS);
        }
        articleLabelMapper.insert(articleLabelVo);
        articleLabelTypeMapper.updateArticleLabelTypeLabelNumAdd(articleLabelVo.getLabelType());
        // 发送博客系统新增文章标签mq消息
        sendSystemData.sendSystemData(SendSystemData.articleLabel, 1);
        return articleLabelVo.getId();
    }

    /**
     * 删除文章标签
     *
     * @param labelIds
     * @param userId
     * @return
     * @throws ServiceException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteArticleLabelByIds(String labelIds, Integer userId) throws ServiceException {
        Set<String> idSet = MyStringUtils.splitString(labelIds, ",");

        List<ArticleLabel> articleLabelList = articleLabelMapper.selectBatchIds(idSet);
        for (ArticleLabel articleLabel : articleLabelList) {
            if (articleLabel.getArticleNum() != 0) {
                String errorMag = "文章标签【" + articleLabel.getLabelName() + "】下文章数量不为0";
                throw new ServiceException(ErrorConstant.ARTICLE_LABEL_NUM_ERROR, errorMag);
            }
            if (!articleLabel.getUserId().equals(userId)) {
                String errorMag = "文章标签【" + articleLabel.getLabelName() + "】创建者不为你";
                throw new ServiceException(ErrorConstant.ARTICLE_LABEL_USER_DELETE_ERROR, errorMag);
            }
        }

        articleLabelMapper.deleteArticleLabelByIds(idSet, userId);
        for (ArticleLabel articleLabel : articleLabelList) {
            articleLabelTypeMapper.updateArticleLabelTypeLabelNumSubtract(articleLabel.getLabelType());
        }

        // 发送博客系统删除文章标签mq消息
        sendSystemData.sendSystemData(SendSystemData.articleLabel, -idSet.size());
        return idSet.size();
    }

    /**
     * 修改文章标签
     *
     * @param articleLabelVo
     * @return
     * @throws ServiceException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateArticleLabel(ArticleLabelVo articleLabelVo) throws ServiceException {
        ArticleLabel oldLabel = articleLabelMapper.selectById(articleLabelVo.getId());
        if (oldLabel == null) {
            throw new ServiceException(ErrorConstant.ARTICLE_LABEL_NOT_EXISTS);
        }
        if (!oldLabel.getUserId().equals(articleLabelVo.getUserId())) {
            throw new ServiceException(ErrorConstant.ARTICLE_LABEL_USER_UPDATE_ERROR);
        }
        articleLabelTypeMapper.updateArticleLabelTypeLabelNumSubtract(oldLabel.getLabelType());
        articleLabelTypeMapper.updateArticleLabelTypeLabelNumAdd(articleLabelVo.getLabelType());
        articleLabelMapper.updateArticleLabel(articleLabelVo);
        return articleLabelVo.getId();
    }

    /**
     * 根据标签分组id查询标签
     *
     * @param labelType
     * @return
     */
    @Override
    public List<ArticleLabel> selectArticleLabelList(Integer labelType) {
        return articleLabelMapper.selectArticleLabelList(labelType);
    }

}
