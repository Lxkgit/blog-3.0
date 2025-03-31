package com.blog.file.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.files.entity.FileData;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 文件服务DAO
 * @Author: lxk
 * @date 2023/8/3 19:58
 */

@Mapper
public interface FileDataDAO extends BaseMapper<FileData> {

    /**
     * 查询系统中全部文章（删除状态的除外）
     * @return
     */
    Integer selectImgCount();

    /**
     * 查询指定的目录数据
     * @param dirPath
     * @return
     */
    FileData selectByPathAndName(@Param("dirPath") String dirPath);

    /**
     * 根据文件id与用户id修改文件编码
     * @param fileData
     */
    void updateFileCodeByIdAndUserId(FileData fileData);
}
