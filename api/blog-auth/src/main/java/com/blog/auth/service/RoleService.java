package com.blog.auth.service;

import com.blog.core.domain.auth.entity.Role;
import com.blog.core.domain.auth.vo.RoleVo;
import com.blog.core.result.ResultPage;

import java.util.List;
import java.util.Map;

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
    ResultPage<Role> selectRoleList(RoleVo role);

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
     * @param roleIds
     * @return
     */
    void deleteRole(String roleIds);

    /**
     * 查询当前角色所有权限菜单id
     * @param roleVo
     * @return
     */
    Map<String, List<Integer>> selectRolePermission(RoleVo roleVo);

    /**
     * 更新角色权限
     * @param roleVo
     */
    void updateRolePermission(RoleVo roleVo);
}
