package com.blog.content.mapper.mybatis;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.content.doc.entity.DocContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author: lxk
 * @date: 2022/6/21 22:38
 * @description:
 * @modified By:
 */

@Mapper
public interface DocContentMapper extends BaseMapper<DocContent> {

    /**
     * 查询文档总数
     * @return
     */
    Integer selectDocContentCount();

//    /**
//     * 以用户id分组查询用户文章数（删除状态的除外）
//     * @return
//     */
//    List<Map<String, Integer>> selectDocCountGroupByUserId();
}
