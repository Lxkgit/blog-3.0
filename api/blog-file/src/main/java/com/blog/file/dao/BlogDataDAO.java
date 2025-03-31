package com.blog.file.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.system.entity.BlogData;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 博客数据统计表
 * @Author: lxk
 * @date 2023/7/10 16:58
 */

@Mapper
public interface BlogDataDAO extends BaseMapper<BlogData> {

    void updateBlogDataById(BlogData blogData);
}
