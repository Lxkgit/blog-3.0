package com.blog.auth.service;

import com.blog.auth.entity.SysUserEntity;

public interface SysUserService {

    /**
     *
     * 根据用户名查询用户信息
     */
    SysUserEntity selectByUsername(String username);

}


