package com.blog.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.blog.content.feign.UserClient;
import com.blog.content.mapper.mybatis.DocCatalogMapper;
import com.blog.content.mapper.mybatis.DocContentMapper;
import com.blog.content.mq.send.SendSystemData;
import com.blog.content.mq.send.SendUserData;
import com.blog.content.service.DocService;
import com.blog.core.constant.Constant;
import com.blog.core.domain.auth.vo.UserVo;
import com.blog.core.domain.content.doc.entity.DocCatalog;
import com.blog.core.domain.content.doc.entity.DocContent;
import com.blog.core.domain.content.doc.enums.DocType;
import com.blog.core.domain.content.doc.vo.DocCatalogVo;
import com.blog.core.utils.SecurityUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: lxk
 * @date 2022/6/20 9:47
 * @description: 文档服务接口
 */

@Slf4j
@Service
public class DocServiceImpl implements DocService {

    @Resource
    private DocCatalogMapper docCatalogMapper;

    @Resource
    private DocContentMapper docContentMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private SendUserData sendUserData;

    @Resource
    private SendSystemData sendSystemData;

    /**
     * 创建文档目录组织 组织类型为目录则只创建目录、组织类型为文档则创建对应文档、文档状态为草稿
     *
     * @param docCatalog
     * @return
     */
    @Override
    public Integer insertDocCatalog(DocCatalog docCatalog) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        docCatalog.setUserId(userId);
        docCatalog.setUpdateTime(new Date());
        docCatalog.setCreateTime(new Date());
        docCatalogMapper.insert(docCatalog);
        if (docCatalog.getDocType().equals(DocType.CONTENT.getId())) {
            DocContent docContent = new DocContent();
            docContent.setCatalogId(docCatalog.getId());
            docContent.setCreateTime(new Date());
            docContent.setUserId(userId);
            docContent.setDocStatus(Constant.DRAFT);
            docContent.setDocContentMd("");
            docContentMapper.insert(docContent);

            // 发送博客用户新增文档mq消息
            sendUserData.sendUserData(SendUserData.doc, userId, 1);
            // 发送博客系统新增文档mq消息
            sendSystemData.sendSystemData(SendSystemData.doc, 1);
        }
        return docCatalog.getId();
    }

    /**
     * 删除文档（修改状态）
     *
     * @param id
     * @return
     */
    @Override
    public Integer deleteDocCatalog(Integer id) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        QueryWrapper<DocCatalog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        queryWrapper.ne("doc_status", Constant.DELETE);
        List<DocCatalog> catalogList = docCatalogMapper.selectList(queryWrapper);
        if (catalogList != null && catalogList.size() > 0) {
            return 0;
        }
        DocCatalog docCatalog = docCatalogMapper.selectById(id);

        // 修改文档目录状态为删除
        DocCatalog catalog = new DocCatalog();
        catalog.setId(id);
        catalog.setDocStatus(Constant.DELETE);
        docCatalogMapper.updateById(catalog);

        if (docCatalog.getDocType().equals(DocType.CONTENT.getId())) {
            // 修改文档状态为删除
            UpdateWrapper<DocContent> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("catalog_id", id);
            DocContent docContent = new DocContent();
            docContent.setDocStatus(Constant.DELETE);
            docContentMapper.update(docContent, updateWrapper);

            // 发送博客用户删除文档mq消息
            sendUserData.sendUserData(SendUserData.doc, userId, -1);
            // 发送博客系统删除文档mq消息
            sendSystemData.sendSystemData(SendSystemData.doc, -1);
        }
        return id;
    }

    /**
     * 修改文档目录
     *
     * @param docCatalog
     * @return
     */
    @Override
    public Integer updateDocCatalog(DocCatalog docCatalog) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        DocCatalog catalog = docCatalogMapper.selectById(docCatalog.getId());
        if (catalog.getUserId().equals(userId)) {
            docCatalog.setUpdateTime(new Date());
            return docCatalogMapper.updateById(docCatalog);
        }
        return 0;
    }

    @Override
    public Integer updateDocContent(DocContent docContent) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        DocContent content = docContentMapper.selectById(docContent.getId());
        if (content.getUserId().equals(userId)) {
            docContent.setUpdateTime(new Date());
            return docContentMapper.updateById(docContent);
        }
        return 0;
    }


    /**
     * 查询文档目录树
     *
     * @param docCatalogVo
     * @return
     */
    @Override
    public List<DocCatalogVo> selectDocCatalogTree(DocCatalogVo docCatalogVo) {
        Integer lowerLimit = docCatalogVo.getTypeLowerLimit();
        Integer upperLimit = docCatalogVo.getTypeUpperLimit();
        List<Integer> docLevelList = new ArrayList<>();
        for (int i = lowerLimit; i <= upperLimit; i++) {
            docLevelList.add(i);
        }
        if (docCatalogVo.getType() == 0) {
            docCatalogVo.setUserId(docCatalogVo.getUserId());
        } else if (docCatalogVo.getType() == 1) {
            Integer userId = SecurityUtil.getLoginUser().getId();
            docCatalogVo.setUserId(userId);
        }
        List<DocCatalogVo> docCatalogVoList = docCatalogMapper.selectListByDocTypeAndUserId(docLevelList, docCatalogVo.getUserId(), docCatalogVo.getDocType());
        if (docCatalogVoList != null) {
            for (DocCatalogVo vo : docCatalogVoList) {
                vo.setValue(vo.getId());
                vo.setLabel(vo.getDocName());
                if (!vo.getDocLevel().equals(lowerLimit)) {
                    docCatalogVoList.forEach(docCatalogVo1 -> {
                        if (docCatalogVo1.getId().equals(vo.getParentId())) {
                            if (docCatalogVo1.getChildren() == null) {
                                docCatalogVo1.setChildren(new ArrayList<>());
                            }
                            docCatalogVo1.getChildren().add(vo);
                        }
                    });
                }
            }
            docCatalogVoList.removeIf(docCatalogVo1 -> !docCatalogVo1.getDocLevel().equals(lowerLimit));
            if (docCatalogVo.getParentId() != null && !docCatalogVo.getParentId().equals(0)) {
                docCatalogVoList.removeIf(docCatalogVo1 -> !docCatalogVo1.getParentId().equals(docCatalogVo.getParentId()));
            }
        }
        return docCatalogVoList;
    }

    /**
     * 查询指定文档
     *
     * @param catalogId
     * @return
     */
    @Override
    public DocContent selectDocContentById(Integer catalogId) {
        QueryWrapper<DocContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("catalog_id", catalogId);
        List<DocContent> docContentList = docContentMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(docContentList)) {
            return docContentList.get(0);
        }
        return null;
    }

    /**
     * 查询指定文档目录
     *
     * @param catalogId
     * @return
     */
    @Override
    public DocCatalog selectDocCatalogById(Integer catalogId) {
        return docCatalogMapper.selectById(catalogId);
    }

    /**
     * 展示文档最多的九位用户
     *
     * @return
     */
    @Override
    public List<UserVo> selectDocUserList() {
        List<Integer> userIdList = docCatalogMapper.selectDocUserList();
        List<UserVo> blogUserList = new ArrayList<>();
        UserVo blogUser = new UserVo();
        blogUser.setId(0);
        blogUser.setUsername("全部用户");
        blogUserList.add(blogUser);
        Map<Integer, UserVo> userMap = new HashMap<>();
        for (Integer userId : userIdList) {
            if (!userMap.containsKey(userId)) {
                userMap.put(userId, userClient.selectUserById(userId));
            }
            blogUserList.add(userMap.get(userId));
        }
        return blogUserList;
    }
}
