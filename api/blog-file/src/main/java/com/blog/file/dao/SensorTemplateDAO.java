package com.blog.file.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.device.entity.SensorTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description 传感器模板持久层
 * @Author lxk
 * @CreateTime 2024-09-29
 */

@Mapper
public interface SensorTemplateDAO  extends BaseMapper<SensorTemplate> {
}
