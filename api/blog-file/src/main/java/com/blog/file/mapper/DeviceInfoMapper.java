package com.blog.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.device.entity.DeviceInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lxk
 * @description 用户设备编码映射表DAO层
 * @date 2024/08/29
 */

@Mapper
public interface DeviceInfoMapper extends BaseMapper<DeviceInfo> {

}
