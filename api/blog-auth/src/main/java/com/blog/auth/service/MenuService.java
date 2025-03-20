package com.blog.auth.service;


//import com.blog.core.entity.auth.vo.SysPermissionVo;


import com.blog.core.domain.auth.vo.MenuVo;

import java.util.List;

/**
 * @Author: lxk
 * @date 2022/6/6 17:10
 * @description:
 */

public interface MenuService {

    List<MenuVo> selectMenuListByUser(MenuVo menuVo);

    List<MenuVo> selectAllMenu(MenuVo menuVo);
}
