package com.blog.content.service;


import com.blog.core.domain.content.article.vo.ArticleLabelTypeVo;
import com.blog.core.exception.ServiceException;

import java.util.List;

/**
 * @author: lxk
 * @date: 2022/6/19 21:32
 * @description:
 * @modified By:
 */
public interface ArticleLabelTypeService {

    Integer saveArticleLabelType(ArticleLabelTypeVo articleLabelTypeVo);

    Integer deleteArticleLabelTypeByIds(String ids) throws ServiceException;

    Integer updateArticleLabelType(ArticleLabelTypeVo articleLabelTypeVo) throws ServiceException;

    List<ArticleLabelTypeVo> getArticleLabelTypeList();
}
