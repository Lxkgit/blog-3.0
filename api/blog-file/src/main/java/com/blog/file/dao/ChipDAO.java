package com.blog.file.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.device.entity.Chip;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * @description: 单片机数据层
 * @Author: lxk
 * @date 2024/1/30 19:58
 */

@Mapper
public interface ChipDAO extends BaseMapper<Chip> {

    void updateChipStatus(@Param("ids") Set<String> ids, @Param("userId") Integer userId, @Param("chipStatus") Integer chipStatus);
}
