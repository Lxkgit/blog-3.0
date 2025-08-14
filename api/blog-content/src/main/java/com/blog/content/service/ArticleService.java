package com.blog.content.service;


import com.blog.core.domain.content.article.entity.Article;
import com.blog.core.domain.content.article.vo.ArticleVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.ResultPage;

public interface ArticleService {

    ResultPage<ArticleVo> selectArticleListByPageAndUserId(ArticleVo articleVo) throws ServiceException;

    ArticleVo selectArticleById(int articleId) throws ServiceException;

    int saveArticle(ArticleVo article) throws ServiceException;

    int updateArticle(Article article) throws ServiceException;

    Integer deleteArticle(String articleIds) throws ServiceException;
}
