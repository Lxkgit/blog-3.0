package com.blog.auth.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.auth.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {


    /**
     * 查看用户对应的角色
     *
     * @param userId 用户id
     * @return 角色权限列表
     */
    List<Role> selectUserRole(@Param("userId") Integer userId);

    /**
     * 删除角色关联的用户
     *
     * @param roleId 角色id
     */
    void deleteRoleUser(@Param("roleId") Integer roleId);

    /**
     * 删除角色关联的菜单
     *
     * @param roleId 角色id
     */
    void deleteRoleMenu(@Param("roleId") Integer roleId);


}
