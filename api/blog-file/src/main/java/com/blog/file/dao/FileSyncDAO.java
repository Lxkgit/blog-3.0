package com.blog.file.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.files.entity.FileSync;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 远程同步文件
 * @Author: lxk
 * @date 2024/1/5 16:07
 */

@Mapper
public interface FileSyncDAO extends BaseMapper<FileSync> {

}
