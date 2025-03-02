package com.dmg.authserver.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dmg.authserver.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 删除用户关联的角色
     * @param id
     * @return
     */
    public Integer deleteRole(@Param("id") Integer id);

    /**
     * 为用户分配角色
     * @return
     */
    public Integer addUserRole(@Param("userId") Integer userId,
                               @Param("roleIds") List<Integer> roleIds);

    /**
     * 查看用户对应的角色id
     * @return
     */
    public List<Integer>getUserRoleIds(@Param("userId") Integer userId);
}