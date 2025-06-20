package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.files.entity.FileSyncLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lxk
 * @description 文件同步日志
 * @date 2025/06/20
 */

@Mapper
public interface FileSyncLogMapper extends BaseMapper<FileSyncLog> {
}
