package com.blog.content.service.impl;


import com.blog.content.feign.UserClient;
import com.blog.content.mapper.mybatis.ArticleTypeMapper;
import com.blog.content.mq.send.SendSystemData;
import com.blog.content.service.ArticleTypeService;
import com.blog.core.constant.ErrorConstant;
import com.blog.core.domain.auth.vo.UserVo;
import com.blog.core.domain.content.article.entity.ArticleType;
import com.blog.core.domain.content.article.vo.ArticleTypeVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: lxk
 * @date 2022/6/14 16:31
 * @description: 文章类型服务类
 */

@Service
public class ArticleTypeServiceImpl implements ArticleTypeService {

    @Resource
    private ArticleTypeMapper articleTypeMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private SendSystemData sendSystemData;

    /**
     * 创建文章分类
     *
     * @param articleTypeVo
     * @return
     */
    @Override
    public Integer saveArticleType(ArticleTypeVo articleTypeVo) {
        articleTypeVo.setId(null);
        articleTypeVo.setNum(0);
        articleTypeVo.setCreateUser(SecurityUtil.getLoginUser().getId());
        articleTypeVo.setCreateTime(new Date());
        articleTypeVo.setUpdateTime(new Date());
        articleTypeMapper.insert(articleTypeVo);

        // 发送博客系统新增文章分类mq消息
        sendSystemData.sendSystemData(SendSystemData.articleType, 1);
        return articleTypeVo.getId();
    }

    /**
     * 删除文章分类
     *
     * @param articleTypeId
     * @return
     */
    @Override
    public Integer deleteArticleTypeById(String articleTypeId) throws ServiceException {
        Set<String> idSet = MyStringUtils.splitString(articleTypeId, ",");
        for (String id : idSet) {
            ArticleType articleType = articleTypeMapper.selectById(Integer.parseInt(id));
            if (articleType != null) {
                if (!articleType.getNum().equals(0)) {
                    throw new ServiceException(ErrorConstant.ARTICLE_TYPE_NUM_ERROR);
                }
            } else {
                throw new ServiceException(ErrorConstant.ARTICLE_TYPE_ERROR, "id: " + id);
            }
        }
        articleTypeMapper.deleteArticleTypeByIds(idSet);
        // 发送博客系统删除文章分类mq消息
        sendSystemData.sendSystemData(SendSystemData.articleType, -idSet.size());
        return idSet.size();
    }

    /**
     * 修改文章分类,文章分类最多支持三级
     *
     * @param articleTypeVo
     * @return
     */
    @Override
    public Integer updateArticleType(ArticleTypeVo articleTypeVo) throws ServiceException {
        Integer parentId = articleTypeVo.getParentId();
        int level = 0;
        while (parentId != 0) {
            ArticleType articleType = articleTypeMapper.selectArticleTypeById(parentId);
            if (articleType != null) {
                parentId = articleType.getParentId();
                level++;
            } else {
                throw new ServiceException(ErrorConstant.ARTICLE_TYPE_PARENT_ERROR);
            }
        }
        if (level > 2) {
            throw new ServiceException(ErrorConstant.ARTICLE_TYPE_LEVEL_ERROR);
        }
        articleTypeVo.setUpdateTime(new Date());
        articleTypeMapper.updateArticleType(articleTypeVo);
        return articleTypeVo.getId();
    }

    /**
     * 根据传入的父节点id获取子节点
     *
     * @param parentId
     * @return
     */
    @Override
    public List<ArticleType> selectArticleTypeByParentId(Integer parentId) {
        return articleTypeMapper.selectArticleTypeByParentId(parentId);
    }

    /**
     * 通过id查询文章分类
     *
     * @param id
     * @return
     */
    @Override
    public List<ArticleType> selectArticleTypeById(Integer id) {
        List<ArticleType> articleTypeList = new ArrayList<>();
        ArticleType articleType = articleTypeMapper.selectArticleTypeById(id);
        articleTypeList.add(articleType);
        if (articleType.getParentId() != 0) {
            articleTypeList.add(articleTypeMapper.selectArticleTypeById(articleType.getParentId()));
        }
        Collections.sort(articleTypeList);
        return articleTypeList;
    }

    /**
     * 查询全部文章分类
     *
     * @return
     */
    @Override
    public List<ArticleType> selectArticleTypeList() {
        return articleTypeMapper.selectList(null);
    }

    /**
     * 获取完整的树接口
     *
     * @return
     */
    @Override
    public List<ArticleTypeVo> selectArticleTypeTree() {
        Map<Integer, UserVo> blogUserMap = new HashMap<>();
        List<ArticleType> articleTypeList = articleTypeMapper.selectList(null);

        List<ArticleTypeVo> articleTypeVoList = new ArrayList<>();
        for (ArticleType articleType : articleTypeList) {
            ArticleTypeVo articleTypeVo = new ArticleTypeVo();
            UserVo blogUser = blogUserMap.get(articleType.getCreateUser());
            if (blogUser == null) {
                blogUser = userClient.selectUserById(articleType.getCreateUser());
                blogUserMap.put(articleType.getCreateUser(), blogUser);
            }
            articleTypeVo.setUserVo(blogUser);
            BeanUtils.copyProperties(articleType, articleTypeVo);
            articleTypeVo.setValue(String.valueOf(articleType.getId()));
            articleTypeVo.setLabel(articleType.getTypeName());
            articleTypeVoList.add(articleTypeVo);
        }
        for (ArticleTypeVo articleTypeVo : articleTypeVoList) {
            if (articleTypeVo.getParentId() != 0) {
                articleTypeVoList.forEach(a -> {
                    if (a.getId().equals(articleTypeVo.getParentId())) {
                        if (a.getChildren() == null) {
                            a.setChildren(new ArrayList<>());
                        }
                        a.getChildren().add(articleTypeVo);
                    }
                });
            }
        }
        articleTypeVoList.removeIf(articleTypeVo -> articleTypeVo.getParentId() != 0);
        return articleTypeVoList;
    }
}
