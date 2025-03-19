package com.blog.auth.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.blog.auth.dao.MenuMapper;
import com.blog.auth.dao.RoleMapper;
import com.blog.auth.service.MenuService;
import com.blog.core.domain.auth.entity.Menu;
import com.blog.core.domain.auth.entity.Role;
import com.blog.core.domain.auth.vo.MenuVo;
import com.blog.core.utils.SecurityUtil;
import jakarta.annotation.Resource;
import org.apache.commons.beanutils.BeanUtils;
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
    private MenuMapper menuDao;

    @Resource
    private RoleMapper roleMapper;


    @Override
    public List<MenuVo> selectMenuList(MenuVo menuVo) {
        Integer userId = SecurityUtil.getLoginUserBo().getId();
        // 查询用户对应角色
        List<Role> roleList = roleMapper.selectUserRole(userId);

        List<Menu> menuList = menuDao.selectUserRole(roleList.stream().map(Role::getId).toList(), menuVo.getMenuType());
        List<MenuVo> menuVoList = BeanUtil.copyToList(menuList, MenuVo.class);
        menuVoList.forEach(item -> item.setChildren(new ArrayList<>()));

        Map<Integer, MenuVo> map = menuVoList.stream().collect(Collectors.toMap(MenuVo::getId, Function.identity(), (m1, m2) -> m1, LinkedHashMap::new));
        List<MenuVo> resultList = new ArrayList<>();
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
