package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.files.entity.FileCategoryData;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 文件服务DAO
 * @Author: lxk
 * @date 2023/8/3 19:58
 */

@Mapper
public interface FileCategoryDataMapper extends BaseMapper<FileCategoryData> {

}
