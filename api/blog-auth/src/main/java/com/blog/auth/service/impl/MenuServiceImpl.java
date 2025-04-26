package com.blog.auth.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.auth.mapper.MenuMapper;
import com.blog.auth.mapper.RoleMapper;
import com.blog.auth.service.MenuService;
import com.blog.core.domain.auth.entity.Menu;
import com.blog.core.domain.auth.entity.Role;
import com.blog.core.domain.auth.vo.MenuVo;
import com.blog.core.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: lxk
 * @date 2022/6/6 17:10
 * @description:
 */

@Service
public class MenuServiceImpl implements MenuService {


    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RoleMapper roleMapper;


    /**
     * 查询用户菜单并以树结构返回
     * @param menuVo 参数 可选 查询菜单层级
     * @return 返回用户菜单树结构
     */
    @Override
    public List<MenuVo> selectMenuListByUser(MenuVo menuVo) {
        Integer userId = SecurityUtil.getLoginUser().getId();
        // 查询用户对应角色
        List<Role> roleList = roleMapper.selectUserRoles(userId);

        // 查询角色的全部菜单
        List<Menu> menuList = menuMapper.selectRoleMenuByRoleIds(roleList.stream().map(Role::getId).toList(), menuVo.getMenuType());
        List<MenuVo> menuVoList = BeanUtil.copyToList(menuList, MenuVo.class);

        return setMenuTree(menuVoList);
    }

    /**
     * 查询全部菜单并以树结构返回
     * @param menuVo 参数 可选 查询菜单层级
     * @return 返回全部菜单树结构
     */
    @Override
    public List<MenuVo> selectAllMenu(MenuVo menuVo) {
        LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(Menu::getMenuType, menuVo.getMenuType());
        queryWrapper.orderByAsc(Menu::getSort);
        List<Menu> menuList = menuMapper.selectList(queryWrapper);
        List<MenuVo> menuVoList = BeanUtil.copyToList(menuList, MenuVo.class);

        return setMenuTree(menuVoList);
    }

    /**
     * 将list结构菜单组装为树结构菜单返回
     * @param menuVoList list结构菜单
     * @return 树结构菜单
     */
    private List<MenuVo> setMenuTree(List<MenuVo> menuVoList) {
        menuVoList.forEach(item -> item.setChildren(new ArrayList<>()));
        // 使用有序map防止组装过程中顺序错乱
        Map<Integer, MenuVo> map = menuVoList.stream().collect(Collectors.toMap(MenuVo::getId, Function.identity(), (m1, m2) -> m1, LinkedHashMap::new));
        List<MenuVo> resultList = new ArrayList<>();
        // parentId 为 0 的数据为组织最上层数据需要返回
        map.forEach((key, value) -> {
            if (value.getParentId() != 0) {
                map.get(value.getParentId()).getChildren().add(value);
            } else {
                resultList.add(value);
            }
        });
        return resultList;
    }

}
