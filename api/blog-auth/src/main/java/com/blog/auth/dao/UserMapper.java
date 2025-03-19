package com.blog.auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 删除用户关联的角色
     *
     * @param id
     * @return
     */
    Integer deleteRole(@Param("id") Integer id);

    /**
     * 为用户分配角色
     *
     * @return
     */
    Integer addUserRole(@Param("userId") Integer userId,
                        @Param("roleIds") List<Integer> roleIds);

    /**
     * 查看用户对应的角色id
     *
     * @return
     */
    List<Integer> getUserRoleIds(@Param("userId") Integer userId);
}