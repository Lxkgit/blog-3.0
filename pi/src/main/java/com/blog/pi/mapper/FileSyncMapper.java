package com.blog.pi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.pi.domain.entity.FileSync;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 文件同步数据库操作接口
 * @Author: lxk
 * @date 2024/1/11 15:08
 */

@Mapper
public interface FileSyncMapper extends BaseMapper<FileSync> {
}
