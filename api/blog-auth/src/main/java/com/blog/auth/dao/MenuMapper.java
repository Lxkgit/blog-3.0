package com.blog.auth.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.core.domain.auth.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {


    /**
     * 查看查看角色下的菜单
     * @param roleIds 角色id列表
     * @param menuType 查找菜单最深层级 1目录 2菜单 3按钮
     * @return
     */
    List<Menu> selectRoleMenuByRoleIds(@Param("roleIds") List<Integer> roleIds, @Param("menuType") Integer menuType);

    /**
     * 查看查看角色下的菜单
     * @param roleId 角色id
     * @return
     */
    List<Menu> selectRoleMenuByRoleId(@Param("roleId") Integer roleId);

    /**
     * 根据菜单id集合 获取对应的权限
     *
     * @param ids
     * @return
     */
    List<Menu> getAuthList(@Param("ids") List<Integer> ids);







    /**
     * 查询用户关联的菜单id
     *
     * @param userId
     * @return
     */
    List<Integer> getMenuIdsByUserId(@Param("userId") Integer userId);

    /**
     * 根据菜单id集合查询父级id
     *
     * @param ids
     * @return
     */
    List<Integer> getPIdsByIds(@Param("ids") List<Integer> ids);
}
