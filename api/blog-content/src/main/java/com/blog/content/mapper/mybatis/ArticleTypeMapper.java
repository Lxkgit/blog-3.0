package com.blog.content.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.content.article.entity.ArticleType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface ArticleTypeMapper extends BaseMapper<ArticleType> {

    ArticleType selectArticleTypeById(@Param("id") Integer id);
    List<ArticleType> selectArticleTypeByParentId(@Param("parentId") Integer parentId);
    List<ArticleType> selectArticleTypeByArray(@Param("array") String[] types);
    int updateArticleType(ArticleType articleType);
    int deleteArticleTypeByIds(@Param("ids") Set<String> ids);
    int deleteArticleTypeByParentId(@Param("parentId") String parentId);
    void updateArticleTypeNumById(@Param("id") Integer id, @Param("articleNum") Integer articleNum);
}
