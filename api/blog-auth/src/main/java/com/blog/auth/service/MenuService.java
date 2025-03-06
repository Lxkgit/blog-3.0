package com.blog.auth.service;


import com.blog.core.entity.auth.SysPermission;
import com.blog.core.entity.auth.vo.SysPermissionVo;

import java.util.List;
import java.util.Set;

/**
 * @Author: lxk
 * @date 2022/6/6 17:10
 * @description:
 */

public interface MenuService {

    Set<SysPermission> selectPermissionByRoleIds(Set<Integer> roleIds, Integer menuType);

    List<SysPermissionVo> selectPermissionListByUserId(Integer userId, Integer menuType);

    List<SysPermissionVo> selectPermissionList(Integer menuType);
}
