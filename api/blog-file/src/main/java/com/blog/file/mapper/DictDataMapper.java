package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.dict.DictData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-12-25
 */

@Mapper
public interface DictDataMapper extends BaseMapper<DictData> {

    void deleteDictDataByDictTypeCode(String dictTypeCode);
}

