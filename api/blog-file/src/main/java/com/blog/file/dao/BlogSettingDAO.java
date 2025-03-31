package com.blog.file.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.system.entity.BlogSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description:
 * @Author: lxk
 * @date 2023/7/25 17:23
 */

@Mapper
public interface BlogSettingDAO extends BaseMapper<BlogSetting> {

}
