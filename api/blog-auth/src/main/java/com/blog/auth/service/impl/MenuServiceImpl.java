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



    @Override
    public List<MenuVo> selectPermissionListByUserId(Integer userId, Integer menuType) {
        List<Role> roleList = roleDao.getUserRole(userId);
        
//        Set<Menu> sysPermissionSet = menuMapper.selectPermissionByRoleIds(sysRoles.stream().map(SysRole::getId).collect(Collectors.toSet()), menuType);
//        List<SysPermissionVo> sysPermissionList = new ArrayList<>();
//        setPermissionTree(sysPermissionSet, sysPermissionList);
//        return sysPermissionList;
    }


    @Resource
    private MenuMapper menuMapper;

//    @Resource
//    private SysRoleService sysRoleService;

//    @Override
//    public Set<SysPermission> selectPermissionByRoleIds(Set<Integer> roleIds, Integer menuType) {
//        return menuMapper.selectPermissionByRoleIds(roleIds, menuType);
//    }
//

//
//    @Override
//    public List<SysPermissionVo> selectPermissionList(Integer menuType) {
//        Set<SysPermission> sysPermissionSet = menuMapper.selectPermissionList(menuType);
//        List<SysPermissionVo> sysPermissionList = new ArrayList<>();
//        setPermissionTree(sysPermissionSet, sysPermissionList);
//        return sysPermissionList;
//    }
//
//    private void setPermissionTree(Set<SysPermission> sysPermissionSet, List<SysPermissionVo> sysPermissionList){
//        for (SysPermission sysPermission : sysPermissionSet) {
//            SysPermissionVo sysPermissionVo = new SysPermissionVo();
//            BeanUtils.copyProperties(sysPermission, sysPermissionVo);
//            sysPermissionVo.setLabel(sysPermission.getMenuName());
//            sysPermissionList.add(sysPermissionVo);
//        }
//
//        for (SysPermissionVo sysPermissionVo : sysPermissionList){
//            if (sysPermissionVo.getParentId()!=0){
//                for (SysPermissionVo permissionVo : sysPermissionList) {
//                    if (permissionVo.getId().equals(sysPermissionVo.getParentId())) {
//                        if (permissionVo.getList()==null){
//                            permissionVo.setList(new ArrayList<>());
//                        }
//                        permissionVo.getList().add(sysPermissionVo);
//                        Collections.sort(permissionVo.getList());
//                        permissionVo.setChildren(permissionVo.getList());
//                        break;
//                    }
//                }
//            }
//        }
//
//        sysPermissionList.removeIf(sysPermissionVo -> sysPermissionVo.getParentId() != 0);
//        Collections.sort(sysPermissionList);
//    }
}
