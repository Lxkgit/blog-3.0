package com.blog.pi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.pi.domain.entity.RegisterSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 注册配置数据层
 * @Author: lxk
 * @date 2024/1/29 15:45
 */

@Mapper
public interface RegisterSettingMapper extends BaseMapper<RegisterSetting> {
}
