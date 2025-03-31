package com.blog.file.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.system.entity.ContentCount;
import io.lettuce.core.dynamic.annotation.Param;

/**
 * @description: 用户内容数量统计表
 * @Author: lxk
 * @date 2023/6/28 17:23
 */

public interface ContentCountMapper extends BaseMapper<ContentCount> {

    Integer selectCountByUserId(@Param("userId") int userId);

    void updateContentCountByUserId(ContentCount contentCount);
}
