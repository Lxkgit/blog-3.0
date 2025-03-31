package com.blog.file.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.file.device.entity.Device;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * @description: 设备数据层
 * @Author: lxk
 * @date 2024/1/29 13:51
 */

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    void updateDeviceStatusById(@Param("id") String id, @Param("userId") Integer userId, @Param("deviceStatus") Integer deviceStatus);
    void updateDeviceStatusByIds(@Param("ids") Set<String> ids, @Param("userId") Integer userId, @Param("deviceStatus") Integer deviceStatus);
}
