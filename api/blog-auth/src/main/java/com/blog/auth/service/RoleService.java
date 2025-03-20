package com.blog.auth.service;

import com.blog.core.domain.auth.entity.Role;
import com.blog.core.domain.auth.vo.RoleVo;
import com.blog.core.result.MyPage;

import java.util.List;

/**
 * @author lxk
 * @description 角色查询相关服务
 * @date 2025/03/20
 */

public interface RoleService {

    /**
     * 根据id查询角色
     * @param id
     * @return
     */
    RoleVo selectRoleById(Integer id);

    /**
     * 分页查询角色列表
     * @param role
     * @return
     */
    MyPage<Role> selectRoleList(RoleVo role);

    /**
     * 新增角色
     * @param role
     * @return
     */
    int insertRole(RoleVo role);

    /**
     * 修改角色
     * @param role
     * @return
     */
    int updateRole(RoleVo role);

    /**
     * 删除角色
     * @param roleId
     * @return
     */
    int deleteRole(Integer roleId);
}
