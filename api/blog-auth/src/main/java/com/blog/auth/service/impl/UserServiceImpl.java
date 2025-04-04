package com.blog.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.blog.auth.mapper.UserMapper;
import com.blog.auth.service.UserService;
import com.blog.core.domain.auth.entity.User;
import com.blog.core.domain.auth.vo.UserVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author lxk
 * @description 用户接口实现类
 * @date 2025/03/24
 */

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public UserVo selectUserById(Integer userId) {
        User user = userMapper.selectById(userId);
        return BeanUtil.copyProperties(user, UserVo.class);
    }
}
