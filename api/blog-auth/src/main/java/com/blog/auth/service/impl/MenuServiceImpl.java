package com.blog.auth.service.impl;


import com.blog.auth.dao.MenuMapper;
import com.blog.auth.dao.RoleMapper;
import com.blog.auth.service.MenuService;
import com.blog.auth.service.SysRoleService;
import com.blog.core.entity.auth.Role;
import com.blog.core.entity.auth.SysRole;
import com.blog.core.entity.auth.vo.MenuVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

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
    private RoleMapper roleDao;




}
