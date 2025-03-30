package com.blog.file.service.impl;

import com.blog.core.domain.file.entity.BlogData;
import com.blog.core.domain.file.vo.BlogDataVo;
import com.blog.file.dao.BlogDataDAO;
import com.blog.file.service.BlogDataService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @description: 博客数据接口服务
 * @Author: lxk
 * @date 2023/7/14 11:35
 */

@Service
public class BlogDataServiceImpl implements BlogDataService {

    @Resource
    private BlogDataDAO blogDataDAO;

    @Override
    public BlogDataVo selectBlogData() {
        BlogData blogData = blogDataDAO.selectById(1);
        BlogDataVo blogDataVo = new BlogDataVo();
        BeanUtils.copyProperties(blogData, blogDataVo);
        return blogDataVo;
    }
}
