package com.blog.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.auth.entity.SysUserEntity;
import com.blog.auth.mapper.SysUserMapper;
import com.blog.auth.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserEntity> implements SysUserService {

    @Override
    public SysUserEntity selectByUsername(String username) {
        LambdaQueryWrapper<SysUserEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUserEntity::getUsername,username);
        return this.getOne(lambdaQueryWrapper);
    }
}


