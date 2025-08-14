package com.blog.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.auth.mapper.MenuMapper;
import com.blog.auth.mapper.RoleMapper;
import com.blog.auth.service.RoleService;
import com.blog.core.domain.auth.entity.Menu;
import com.blog.core.domain.auth.entity.Role;
import com.blog.core.domain.auth.vo.RoleVo;
import com.blog.core.result.ResultPage;
import com.blog.core.result.ResultPageUtils;
import com.blog.core.utils.SecurityUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 角色服务实现类
 * @Author lxk
 * @CreateTime 2025-03-20
 */

@Service
public class RoleServiceImpl implements RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private MenuMapper menuMapper;

    @Override
    public RoleVo selectRoleById(Integer id) {
        return null;
    }

    @Override
    public ResultPage<Role> selectRoleList(RoleVo role) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        PageHelper.startPage(role.getPageNum(), role.getPageSize());
        List<Role> roles = roleMapper.selectList(queryWrapper);
        return ResultPageUtils.pageUtil(roles, role.getPageNum(), role.getPageSize(), new PageInfo<>(roles).getTotal());
    }

    @Override
    public int insertRole(RoleVo role) {
        role.setCreateBy(SecurityUtil.getLoginUser().getUsername());
        role.setCreateTime(new Date());
        roleMapper.insert(role);
        return role.getId();
    }

    @Override
    public int updateRole(RoleVo role) {
        role.setUpdateBy(SecurityUtil.getLoginUser().getUsername());
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

    @Override
    public Map<String, List<Integer>> selectRolePermission(RoleVo roleVo) {
        List<Menu> menuList = menuMapper.selectRoleMenuByRoleId(roleVo.getId());
        Map<String, List<Integer>> resultMap = new HashMap<>();
        resultMap.put("perIds", menuList.stream().map(Menu::getId).toList());
        return resultMap;
    }

    @Override
    public void updateRolePermission(RoleVo roleVo) {
        roleMapper.deleteRoleMenu(roleVo.getId());
        if (CollectionUtils.isNotEmpty(roleVo.getMenuIds())) {
            roleMapper.insertRoleMenus(roleVo.getId(), roleVo.getMenuIds());
        }
    }
}
