package com.blog.content.service;



import com.blog.core.domain.content.article.entity.ArticleLabel;
import com.blog.core.domain.content.article.vo.ArticleLabelVo;
import com.blog.core.exception.ValidException;

import java.util.List;

public interface ArticleLabelService {

    Integer saveArticleLabel(ArticleLabelVo articleLabelVo) throws ValidException;
    Integer deleteArticleLabelByIds(String ids, Integer userId) throws ValidException;
    Integer updateArticleLabel(ArticleLabelVo articleLabelVo) throws ValidException;
    List<ArticleLabel> selectArticleLabelList(Integer userId);
}
