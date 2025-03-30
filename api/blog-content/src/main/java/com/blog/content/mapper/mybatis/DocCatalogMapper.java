package com.blog.content.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.content.doc.entity.DocCatalog;
import com.blog.core.domain.content.doc.vo.DocCatalogVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocCatalogMapper extends BaseMapper<DocCatalog> {

    List<DocCatalogVo> selectListByDocTypeAndUserId(@Param("docLevelList") List<Integer> docLevelList, @Param("userId") Integer userId, @Param("docType") Integer docType);
    List<Integer> selectDocUserList();

}