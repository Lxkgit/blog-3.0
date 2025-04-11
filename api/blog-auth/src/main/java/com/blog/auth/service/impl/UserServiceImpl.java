package com.blog.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.blog.auth.mapper.UserMapper;
import com.blog.auth.service.UserService;
import com.blog.core.domain.auth.entity.User;
import com.blog.core.domain.auth.vo.UserVo;
import com.blog.core.domain.content.article.entity.Article;
import com.blog.core.result.MyPage;
import com.blog.core.result.MyPageUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lxk
 * @description 用户接口实现类
 * @date 2025/03/24
 */

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    @Resource
    private UserMapper userMapper;

    @Override
    public UserVo selectUserById(Integer userId) {
        User user = userMapper.selectById(userId);
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
    public MyPage<UserVo> selectUserByPage(UserVo userVo) {

        PageHelper.startPage(userVo.getPageNum(), userVo.getPageSize());
        List<User> userList = userMapper.selectList(new QueryWrapper<>());
        PageInfo<User> articlePage = new PageInfo<>(userList);
        List<UserVo> userVoList = BeanUtil.copyToList(articlePage.getList(), UserVo.class);

        MyPage<UserVo> myPage = null;
        try {
            myPage = MyPageUtils.pageUtil(userVoList, userVo.getPageNum(), userVo.getPageSize(), (int) articlePage.getTotal());
        } catch (Exception e){
            log.info(e.getMessage());
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
}
