package com.blog.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.content.feign.UserClient;
import com.blog.content.mapper.mybatis.ArticleMapper;
import com.blog.content.mapper.mybatis.ArticleLabelMapper;
import com.blog.content.mapper.mybatis.ArticleTypeMapper;
import com.blog.content.mq.send.SendSystemData;
import com.blog.content.mq.send.SendUserData;
import com.blog.content.service.ArticleService;
import com.blog.core.constant.Constant;
import com.blog.core.constant.ErrorConstant;
import com.blog.core.domain.auth.vo.UserVo;
import com.blog.core.domain.content.article.bo.ArticleBo;
import com.blog.core.domain.content.article.entity.Article;
import com.blog.core.domain.content.article.entity.ArticleLabel;
import com.blog.core.domain.content.article.entity.ArticleType;
import com.blog.core.domain.content.article.vo.ArticleVo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.ResultPage;
import com.blog.core.result.ResultPageUtils;
import com.blog.core.utils.MyStringUtils;
import com.blog.core.utils.SecurityUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: lxk
 * @date 2022/6/9 9:33
 * @description: 文章服务
 */

@Service
public class ArticleServiceImpl implements ArticleService {

    @Resource
    private UserClient userClient;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ArticleTypeMapper articleTypeMapper;

    @Resource
    private ArticleLabelMapper articleLabelMapper;

    @Resource
    private SendSystemData sendSystemData;

    @Resource
    private SendUserData sendUserData;

    /**
     * 创建文章
     *
     * @param articleVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveArticle(ArticleVo articleVo) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();

        // 初始化对象，防止页面携带数据
        articleVo.setId(null);
        articleVo.setUserId(userId);
        articleVo.setUpdateTime(new Date());
        articleVo.setCreateTime(new Date());
        articleVo.setBrowseCount(0);
        articleVo.setLikeCount(0);

        // 文章类型页面传入数据是类型树最底层节点id
        articleVo.setArticleType(updateArticleType(articleVo.getArticleType(), 1));
        updateArticleLabel(articleVo.getArticleLabel(), 1);

        articleMapper.insert(articleVo);

        // 发送博客用户新增文章mq消息
        sendUserData.sendUserData(SendUserData.article, userId, 1);
        // 发送博客系统新增文章mq消息
        sendSystemData.sendSystemData(SendSystemData.article, 1);
        return articleVo.getId();
    }

    /**
     * 删除文章, 并非真正删除，修改文章状态为删除
     *
     * @param articleIds
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteArticle(String articleIds) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();

        ArticleBo articleBo = new ArticleBo();
        articleBo.setUserId(userId);
        articleBo.setArticleStatus(Constant.DELETE);
        Set<String> idSet = MyStringUtils.splitString(articleIds, ",");
        articleBo.setIds(idSet);
        // 假删除 修改文章状态为删除状态 3
        Integer deleteArticleNum = articleMapper.updateArticleStatus(articleBo);

        for (String id : articleBo.getIds()) {
            Article article = articleMapper.selectById(Integer.parseInt(id));
            updateArticleType(article.getArticleType(), -1);
            updateArticleLabel(article.getArticleLabel(), -1);
        }

        // 发送博客用户删除文章mq消息
        sendUserData.sendUserData(SendUserData.article, userId, -deleteArticleNum);
        // 发送博客系统删除文章mq消息
        sendSystemData.sendSystemData(SendSystemData.article, -deleteArticleNum);

        return null;

    }

    /**
     * 更新文章接口
     *
     * @param article 文章数据
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateArticle(Article article) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();

        ArticleBo articleBo = new ArticleBo();
        Article oldArticle = articleMapper.selectById(article.getId());
        if (oldArticle == null) {
            throw new ServiceException(ErrorConstant.ARTICLE_NULL);
        }

        // 修改前文章分类-1
        updateArticleType(oldArticle.getArticleType(), -1);
        // 修改前文章标签对应文章-1
        updateArticleLabel(oldArticle.getArticleLabel(), -1);

        // 修改后文章分类+1
        article.setArticleType(updateArticleType(article.getArticleType(), 1));
        // 修改后文章标签对应文章+1
        updateArticleLabel(article.getArticleLabel(), 1);

        BeanUtils.copyProperties(article, articleBo);
        articleBo.setUpdateUserId(userId);
        articleBo.setUpdateTime(new Date());
        articleMapper.updateArticle(articleBo);
        return 0;
    }

    /**
     * 查询文章列表
     *
     * @param param 查询参数
     * @return
     */
    @Override
    public ResultPage<ArticleVo> selectArticleListByPageAndUserId(ArticleVo param) throws ServiceException {

        QueryWrapper<Article> articleQueryWrapper = new QueryWrapper<>();

        // 管理页面只查询当前用户文章，首页查询全部和指定用户文章
        if (param.getType() == 1) {
            Integer userId = SecurityUtil.getLoginUser().getId();
            articleQueryWrapper.eq("user_id", userId);
        } else {
            if (param.getSelectUser() != null && param.getSelectUser() != 0) {
                articleQueryWrapper.eq("user_id", param.getSelectUser());
            }
        }

        // 按照文章分类查询
        if (StringUtils.isNotEmpty(param.getArticleType())) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(param.getArticleType());
            ArticleType articleType = articleTypeMapper.selectById(Integer.parseInt(param.getArticleType()));
            if (articleType == null) {
                throw new ServiceException(ErrorConstant.ARTICLE_TYPE_ERROR);
            }
            while (articleType.getParentId() != 0) {
                articleType = articleTypeMapper.selectById(articleType.getParentId());
                stringBuilder.insert(0, articleType.getId() + ",");
            }
            articleQueryWrapper.likeRight("article_type", stringBuilder.toString());
        }

        // 指定文章状态
        if (StringUtils.isNotEmpty(param.getSelectStatus())) {
            Set<String> statusSet = MyStringUtils.splitString(param.getSelectStatus(), ",");
            articleQueryWrapper.and((wrapper) -> {
                Iterator<String> set = statusSet.iterator();
                int i = 0;
                while (set.hasNext()) {
                    if (i == 0) {
                        wrapper.eq("article_status", set.next());
                    } else {
                        wrapper.or().eq("article_status", set.next());
                    }
                    set.remove();
                    i++;
                }
            });
        }

        // 排序
        if (StringUtils.isNotEmpty(param.getSortType())) {
            List<String> sortList = Arrays.asList(param.getSortType().split(","));
            if (sortList.contains("0")) {
                articleQueryWrapper.orderByDesc("article_status");
            }
            if (sortList.contains("1")) {
                articleQueryWrapper.orderByDesc("update_time");
            }
        }

        PageHelper.startPage(param.getPageNum(), param.getPageSize());
        List<Article> articleList = articleMapper.selectList(articleQueryWrapper);
        PageInfo<Article> articlePage = new PageInfo<>(articleList);
        List<ArticleVo> articleVoList = new ArrayList<>();

        Map<Integer, UserVo> userMap = new HashMap<>();
        for (Article article : articlePage.getList()) {
            ArticleVo articleVo = new ArticleVo();
            BeanUtils.copyProperties(article, articleVo);
            if (userMap.containsKey(article.getUserId())) {
                articleVo.setUserVo(userMap.get(article.getUserId()));
            } else {
//                UserVo userVo = userClient.selectUserById(article.getUserId());
//                userMap.put(article.getUserId(), userVo);
//                articleVo.setUserVo(userVo);
            }
            setArticleTypeAndLabel(article, articleVo);
            articleVoList.add(articleVo);
        }
        return ResultPageUtils.pageUtil(articleVoList, articlePage.getPageNum(), articlePage.getPageSize(), (int) articlePage.getTotal());
    }

    /**
     * 查询文章分类和标签
     *
     * @param article
     * @param articleVo
     */
    private void setArticleTypeAndLabel(Article article, ArticleVo articleVo) {
        if (StringUtils.isNotEmpty(article.getArticleType())) {
            String[] types = article.getArticleType().split(",");
            List<ArticleType> articleTypeList = articleTypeMapper.selectArticleTypeByArray(types);
            articleVo.setArticleTypes(articleTypeList);
        }
        if (StringUtils.isNotEmpty(article.getArticleLabel())) {
            String[] labels = article.getArticleLabel().split(",");
            List<ArticleLabel> articleLabelList = articleLabelMapper.selectArticleLabelByArray(labels);
            articleVo.setArticleLabels(articleLabelList);
        }
    }

    /**
     * 根据id查询文章
     *
     * @param articleId
     * @return
     */
    @Override
    public ArticleVo selectArticleById(int articleId) throws ServiceException {
        Article article = articleMapper.selectById(articleId);
        if(article == null) {
            throw new ServiceException("文章不存在");
        }
        ArticleVo articleVo = new ArticleVo();
        BeanUtils.copyProperties(article, articleVo);
        UserVo userVo = userClient.selectUserById(article.getUserId());
        articleVo.setUserVo(userVo);
        setArticleTypeAndLabel(article, articleVo);
        return articleVo;
    }


    /**
     * 修改文章分类下文章数量，返回文章分类全类型字符串（从根节点分类开始）
     * 传入文章分类字符串 单个分类则从该分类找到分类根节点 对每一个分类执行文章数量变化
     * 多个分类按照由高到低取最后一个文章的最底层分类然后按照单分类处理
     *
     * @param articleTypes 文章分类 文章分类id 多个id使用 , 分隔
     * @param articleNum   文章数量变化
     * @return
     */
    private String updateArticleType(String articleTypes, Integer articleNum) throws ServiceException {
        if (StringUtils.isNotBlank(articleTypes)) {
            String[] articleTypeArr = articleTypes.split(",");
            StringBuilder type = new StringBuilder(articleTypeArr[articleTypeArr.length - 1]);
            ArticleType type1 = articleTypeMapper.selectArticleTypeById(Integer.parseInt(String.valueOf(type)));
            if (type1 == null) {
                throw new ServiceException(ErrorConstant.ARTICLE_TYPE_ERROR);
            }
            while (type1.getParentId() != 0) {
                type.insert(0, type1.getParentId() + ",");
                articleTypeMapper.updateArticleTypeNumById(type1.getId(), articleNum);
                type1 = articleTypeMapper.selectArticleTypeById(type1.getParentId());
            }
            articleTypeMapper.updateArticleTypeNumById(type1.getId(), articleNum);
            return type.toString();
        }
        return null;
    }

    /**
     * 更新文章标签下文章数量
     *
     * @param articleLabel 文章标签id 多个id使用 , 分隔
     * @param articleNum   文章数量 用于计算标签下文章加减数值
     */
    private void updateArticleLabel(String articleLabel, Integer articleNum) throws ServiceException {
        if (StringUtils.isNotBlank(articleLabel)) {
            Set<String> labelSet = Arrays.stream(articleLabel.split(",")).collect(Collectors.toSet());
            for (String label : labelSet) {
                Integer id = Integer.parseInt(label);
                ArticleLabel articleLabel1 = articleLabelMapper.selectById(id);
                if (articleLabel1 == null) {
                    throw new ServiceException(ErrorConstant.ARTICLE_LABEL_NOT_EXISTS);
                }
                articleLabelMapper.updateArticleLabelNumById(Integer.parseInt(label), articleNum);
            }
        }
    }


}
