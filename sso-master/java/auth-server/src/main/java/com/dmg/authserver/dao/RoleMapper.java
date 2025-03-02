package com.dmg.authserver.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dmg.authserver.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper  extends BaseMapper<Role> {

    /**
     * 删除角色关联的用户
     * @param id
     * @return
     */
    Integer deleteUser(@Param("id") Integer id);

    /**
     * 删除角色关联的权限
     * @param id
     * @return
     */
    Integer deleteAuth(@Param("id") Integer id);

    /**
     * 查看用户对应的角色
     * @param userId
     * @return
     */
    public List<Role> getUserRole(@Param("userId") Integer userId);

    /**
     * 为角色分配权限
     * @return
     */
    public Integer addRoleAuth(@Param("roleId") Integer roleId,
                               @Param("menuIds") List<Integer> menuIds);
}
