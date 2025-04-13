package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.files.entity.FileCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lxk
 * @description 文件目录Mapper
 * @date 2025/04/13
 */

@Mapper
public interface FileCategoryMapper extends BaseMapper<FileCategory> {
}
