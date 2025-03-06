package com.blog.auth.service;




import com.blog.core.entity.auth.SysRole;
import com.blog.core.entity.auth.vo.SysRoleVo;
import com.blog.core.utils.MyPage;

import java.util.Map;
import java.util.Set;

public interface SysRoleService {
    Set<SysRole> selectRoleByUserId(Integer userId);
    MyPage<SysRole> selectRoleByPage(int page, int size);
    int saveRole(SysRole sysRole);
    int updateRole(SysRole sysRole);
    Map<String, Object> selectRolePermission(Integer roleId, Integer menuType);
    Map<String, Object> deleteRoleByIds(String ids);
    Map<String, Object> updateRolePermission(SysRoleVo sysRoleVo);
}
