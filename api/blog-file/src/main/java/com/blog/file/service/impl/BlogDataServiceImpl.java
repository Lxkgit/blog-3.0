package com.blog.file.service.impl;

import com.blog.core.domain.file.system.entity.BlogData;
import com.blog.core.domain.file.system.vo.BlogDataVo;
import com.blog.file.mapper.BlogDataMapper;
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
    private BlogDataMapper blogDataMapper;

    @Override
    public BlogDataVo selectBlogData() {
        BlogData blogData = blogDataMapper.selectById(1);
        BlogDataVo blogDataVo = new BlogDataVo();
        BeanUtils.copyProperties(blogData, blogDataVo);
        return blogDataVo;
    }
}
