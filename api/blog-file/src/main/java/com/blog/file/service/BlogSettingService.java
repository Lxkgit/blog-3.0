package com.blog.file.service;


import com.blog.core.domain.file.system.vo.BlogSettingVo;

import java.util.List;

/**
 * @description: 博客设置
 * @Author: lxk
 * @date 2023/7/25 17:26
 */

public interface BlogSettingService {

    BlogSettingVo selectBlogSettingById(Integer id);

    List<BlogSettingVo> selectBlogSetting(String type);

    void updateBlogSetting(BlogSettingVo blogSettingVo);
}
