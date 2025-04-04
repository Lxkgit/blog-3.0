package com.blog.content.service;


import com.blog.core.domain.content.article.entity.ArticleLabel;
import com.blog.core.domain.content.article.vo.ArticleLabelVo;
import com.blog.core.exception.ServiceException;

import java.util.List;

public interface ArticleLabelService {

    Integer saveArticleLabel(ArticleLabelVo articleLabelVo) throws ServiceException;

    Integer deleteArticleLabelByIds(String ids, Integer userId) throws ServiceException;

    Integer updateArticleLabel(ArticleLabelVo articleLabelVo) throws ServiceException;

    List<ArticleLabel> selectArticleLabelList(Integer userId);
}
