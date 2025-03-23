package com.blog.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.auth.dao.RoleMapper;
import com.blog.auth.service.RoleService;
import com.blog.core.domain.auth.entity.Role;
import com.blog.core.domain.auth.vo.RoleVo;
import com.blog.core.result.MyPage;
import com.blog.core.result.MyPageUtils;
import com.blog.core.utils.SecurityUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Description 角色服务实现类
 * @Author lxk
 * @CreateTime 2025-03-20
 */

@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Override
    public RoleVo selectRoleById(Integer id) {
        return null;
    }

    @Override
    public MyPage<Role> selectRoleList(RoleVo role) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        PageHelper.startPage(role.getPageNum(), role.getPageSize());
        List<Role> roles = roleMapper.selectList(queryWrapper);
        Long count  = roleMapper.selectCount(queryWrapper);
        return MyPageUtils.pageUtil(roles, role.getPageNum(), role.getPageSize(), count);
    }

    @Override
    public int insertRole(RoleVo role) {
        role.setCreateBy(SecurityUtil.getLoginUserBo().getUsername());
        role.setCreateTime(new Date());
        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    public int updateRole(RoleVo role) {
        role.setUpdateBy(SecurityUtil.getLoginUserBo().getUsername());
        role.setUpdateTime(new Date());
        roleMapper.updateById(role);
        return role.getId();
    }

    @Override
    public int deleteRole(Integer roleId) {
        roleMapper.deleteRoleMenu(roleId);
        roleMapper.deleteRoleUser(roleId);
        return roleMapper.deleteById(roleId);
    }
}
