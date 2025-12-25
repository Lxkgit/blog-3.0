package com.blog.file.mapper;

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
public interface DictDataMapper {

    DictData selectById(@Param("id") Integer id);

    List<DictData> selectByDictTypeCode(@Param("dictTypeCode") String dictTypeCode);

    List<DictData> selectList(DictData dictData);

    int insert(DictData dictData);

    int update(DictData dictData);

    int deleteById(@Param("id") Integer id);
}

