package com.blog.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.auth.mapper.RoleMapper;
import com.blog.auth.mapper.UserMapper;
import com.blog.auth.service.UserService;
import com.blog.core.domain.auth.entity.User;
import com.blog.core.domain.auth.vo.UserVo;
import com.blog.core.result.ResultPage;
import com.blog.core.result.ResultPageUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lxk
 * @description 用户接口实现类
 * @date 2025/03/24
 */

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    public UserVo selectUserById(Integer userId) {
        User user = userMapper.selectById(userId);
        user.setPassword(null);
        return BeanUtil.copyProperties(user, UserVo.class);
    }

    @Override
    public UserVo selectUserByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);
        return BeanUtil.copyProperties(user, UserVo.class);
    }

    @Override
    public ResultPage<UserVo> selectUserByPage(UserVo userVo) {
        ResultPage<UserVo> myPage = null;
        try {
            PageHelper.startPage(userVo.getPageNum(), userVo.getPageSize());
            List<User> userList = userMapper.selectList(new QueryWrapper<>());
            PageInfo<User> articlePage = new PageInfo<>(userList);
            List<UserVo> userVoList = BeanUtil.copyToList(articlePage.getList(), UserVo.class);
            userVoList.forEach(item -> {
                item.setPassword(null);
                item.setRoleList(roleMapper.selectUserRoles(item.getId()));
            });
            myPage = ResultPageUtils.pageUtil(userVoList, userVo.getPageNum(), userVo.getPageSize(), (int) articlePage.getTotal());
        } catch (Exception e) {
            logger.error("用户列表接口查询异常：{}", e.getMessage(), e);
        }
        return myPage;
    }

    @Override
    public void updateUser(UserVo userVo, Integer perFlag) {
        userMapper.updateById(userVo);
        if (perFlag == 1) {
//            sysUserDAO.deleteUserRole(userVo.getId());
//            sysUserDAO.insertUserRole(userVo.getRoleIds(), userVo.getId());
        }
    }

    @Override
    public void updateUserPermission(UserVo userVo) {
        userVo.setPassword(null);
        userMapper.updateById(userVo);
        userMapper.deleteRole(userVo.getId());
        if (CollectionUtils.isNotEmpty(userVo.getRoleIds())) {
            userMapper.insertUserRoles(userVo.getId(), userVo.getRoleIds());
        }
    }
}
