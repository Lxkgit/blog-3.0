package com.blog.content.mapper.mybatis;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.content.article.entity.Article;
import com.blog.core.domain.content.article.bo.ArticleBo;
import com.blog.core.domain.content.article.vo.ArticleVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 查询系统中全部文章（删除状态的除外）
     *
     * @return
     */
    Integer selectArticleCount();

//    /**
//     * 以用户id分组查询用户文章数（删除状态的除外）
//     *
//     * @return
//     */
//    List<Map<String, Integer>> selectArticleCountGroupByUserId();

    /**
     * 分页查询文章
     *
     * @param articleVo
     * @return
     */
    List<Article> selectArticleListByPage(ArticleVo articleVo);

    /**
     *  根据id与用户id修改文章
     * @param articleBo
     * @return
     */
    Integer updateArticle(ArticleBo articleBo);

    /**
     * 修改文章状态
     * @param articleBo
     * @return
     */
    Integer updateArticleStatus(ArticleBo articleBo);


}
