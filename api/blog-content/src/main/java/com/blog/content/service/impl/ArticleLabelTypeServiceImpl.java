package com.blog.content.service.impl;

import com.blog.content.feign.UserClient;
import com.blog.content.mapper.mybatis.ArticleLabelMapper;
import com.blog.content.mapper.mybatis.ArticleLabelTypeMapper;
import com.blog.content.service.ArticleLabelTypeService;
import com.blog.core.constant.ErrorConstant;
import com.blog.core.domain.auth.vo.UserVo;
import com.blog.core.domain.content.article.entity.ArticleLabel;
import com.blog.core.domain.content.article.entity.ArticleLabelType;
import com.blog.core.domain.content.article.vo.ArticleLabelTypeVo;
import com.blog.core.domain.content.article.vo.ArticleLabelVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: lxk
 * @date: 2022/6/19 21:32
 * @description:
 * @modified By:
 */
@Service
public class ArticleLabelTypeServiceImpl implements ArticleLabelTypeService {

    @Resource
    private ArticleLabelMapper articleLabelMapper;

    @Resource
    private ArticleLabelTypeMapper articleLabelTypeMapper;

    @Resource
    private UserClient userClient;

    /**
     * 新增标签分类
     *
     * @param articleLabelTypeVo
     * @return
     */
    @Override
    public Integer saveArticleLabelType(ArticleLabelTypeVo articleLabelTypeVo) {
        articleLabelTypeVo.setId(null);
        articleLabelTypeVo.setUserId(SecurityUtil.getLoginUser().getId());
        articleLabelTypeVo.setLabelNum(0);
        articleLabelTypeVo.setCreateTime(new Date());
        articleLabelTypeVo.setUpdateTime(new Date());
        articleLabelTypeMapper.insert(articleLabelTypeVo);
        return articleLabelTypeVo.getId();
    }

    /**
     * 删除标签分类
     *
     * @param articleLabelTypeIds
     * @return
     * @throws ServiceException
     */
    @Override
    public Integer deleteArticleLabelTypeByIds(String articleLabelTypeIds) throws ServiceException {
        Set<String> ids = MyStringUtils.splitString(articleLabelTypeIds, ",");
        for (String id : ids) {
            ArticleLabelType articleLabelType = articleLabelTypeMapper.selectById(Integer.parseInt(id));
            if (!articleLabelType.getLabelNum().equals(0)) {
                throw new ServiceException(ErrorConstant.ARTICLE_LABEL_TYPE_NUMBER_ERROR);
            }
        }
        articleLabelTypeMapper.deleteArticleLabelTypeByIds(ids);
        return ids.size();
    }

    /**
     * 修改标签分类
     *
     * @param articleLabelTypeVo
     * @return
     */
    @Override
    public Integer updateArticleLabelType(ArticleLabelTypeVo articleLabelTypeVo) throws ServiceException {
        ArticleLabelType articleLabelType = articleLabelTypeMapper.selectById(articleLabelTypeVo.getId());
        if (articleLabelType == null) {
            throw new ServiceException(ErrorConstant.ARTICLE_LABEL_TYPE_NOT_EXISTS);
        }
        articleLabelTypeVo.setUpdateTime(new Date());
        articleLabelTypeMapper.updateArticleLabelType(articleLabelTypeVo);
        return articleLabelTypeVo.getId();
    }


    /**
     * 查询标签分类列表
     *
     * @return
     */
    @Override
    public List<ArticleLabelTypeVo> getArticleLabelTypeList() {
        Map<Integer, UserVo> blogUserMap = new HashMap<>();
        List<ArticleLabelTypeVo> articleLabelTypeVoList = new ArrayList<>();
        List<ArticleLabelType> articleLabelTypeList = articleLabelTypeMapper.selectArticleLabelTypeList();
        for (ArticleLabelType articleLabelType : articleLabelTypeList) {
            ArticleLabelTypeVo articleLabelTypeVo = new ArticleLabelTypeVo();
            UserVo labelTypeUser = blogUserMap.get(articleLabelType.getUserId());
            if (labelTypeUser == null) {
                blogUserMap.put(articleLabelType.getUserId(), userClient.selectUserById(articleLabelType.getUserId()));
            }

            List<ArticleLabel> articleLabelList = articleLabelMapper.selectArticleLabelList(articleLabelType.getId());
            List<ArticleLabelVo> articleLabelListVo = new ArrayList<>();
            for (ArticleLabel articleLabel : articleLabelList) {
                ArticleLabelVo articleLabelVo = new ArticleLabelVo();
                BeanUtils.copyProperties(articleLabel, articleLabelVo);
                articleLabelListVo.add(articleLabelVo);
            }
            articleLabelListVo.forEach(item -> {
                UserVo labelUser = blogUserMap.get(item.getUserId());
                if (labelUser == null) {
                    blogUserMap.put(item.getUserId(), userClient.selectUserById(item.getUserId()));
                }
                item.setUserVo(blogUserMap.get(item.getUserId()));
            });
            articleLabelTypeVo.setLabelList(articleLabelListVo);
            articleLabelTypeVo.setValue(articleLabelType.getId());
            articleLabelTypeVo.setLabel(articleLabelType.getTypeName());
            articleLabelTypeVo.setUserVo(blogUserMap.get(articleLabelType.getUserId()));
            BeanUtils.copyProperties(articleLabelType, articleLabelTypeVo);
            articleLabelTypeVoList.add(articleLabelTypeVo);
        }
        return articleLabelTypeVoList;
    }


}
