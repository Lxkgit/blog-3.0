package com.blog.content.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.content.article.entity.ArticleLabel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface ArticleLabelMapper extends BaseMapper<ArticleLabel> {

    /**
     * 查询文章标签总数
     * @return
     */
    Integer selectArticleLabelCount();
    List<ArticleLabel> selectArticleLabelList(@Param("labelType") Integer labelType);
    List<ArticleLabel> selectArticleLabelByArray(@Param("array") String[] labels);
    int updateArticleLabel(ArticleLabel articleLabel);
    int deleteArticleLabelByIds(@Param("ids") Set<String> ids, @Param("userId") Integer userId);
    void updateArticleLabelNumById(@Param("id") Integer id, @Param("articleNum") Integer articleNum);
}
