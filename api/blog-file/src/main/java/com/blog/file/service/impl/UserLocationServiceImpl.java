package com.blog.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.ip.entity.IpLocation;
import com.blog.file.mapper.IpLocationMapper;
import com.blog.file.service.UserLocationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 用户ip定位服务
 * @Author lxk
 * @CreateTime 2026-09-24
 */

@Service
public class UserLocationServiceImpl implements UserLocationService {

    @Resource
    private IpLocationMapper ipLocationMapper;

    @Override
    public List<IpLocation> selectIpLocationList() {
        LambdaQueryWrapper<IpLocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(IpLocation::getId);
        wrapper.last("limit 100");
        return ipLocationMapper.selectList(wrapper);
    }
}
