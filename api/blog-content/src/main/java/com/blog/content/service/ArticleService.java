package com.blog.content.service;


import com.blog.core.domain.content.article.Article;
import com.blog.core.domain.content.article.vo.ArticleVo;
import com.blog.core.exception.ValidException;
import com.blog.core.result.MyPage;

public interface ArticleService {

    MyPage<ArticleVo> selectArticleListByPageAndUserId(ArticleVo articleVo) throws ValidException;
    ArticleVo selectArticleById(int articleId);
    int saveArticle(ArticleVo article) throws ValidException;
//    int updateArticle(BlogUser blogUser, Article article) throws ValidException;
//    Integer deleteArticle(BlogUser blogUser, String articleIds) throws ValidException;
}
