package com.blog.file.mapper;

import com.blog.core.domain.file.dict.DictType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lxk
 * @description
 * @date 2025/12/25
 */

@Mapper
public interface DictTypeMapper {

    DictType selectById(@Param("id") Integer id);

    DictType selectByCode(@Param("dictTypeCode") String dictTypeCode);

    List<DictType> selectList(DictType dictType);

    int insert(DictType dictType);

    int update(DictType dictType);

    int deleteById(@Param("id") Integer id);
}

