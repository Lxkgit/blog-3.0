package com.blog.content.service;



import com.blog.core.domain.content.article.vo.ArticleLabelTypeVo;
import com.blog.core.exception.ValidException;

import java.util.List;

/**
 * @author: lxk
 * @date: 2022/6/19 21:32
 * @description:
 * @modified By:
 */
public interface ArticleLabelTypeService {

    Integer saveArticleLabelType(ArticleLabelTypeVo articleLabelTypeVo);
    Integer deleteArticleLabelTypeByIds(String ids) throws ValidException;
    Integer updateArticleLabelType(ArticleLabelTypeVo articleLabelTypeVo) throws ValidException;
    List<ArticleLabelTypeVo> getArticleLabelTypeList();
}
