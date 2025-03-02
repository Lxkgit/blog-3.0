package com.dmg.authserver.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dmg.authserver.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {


    /**
     * 根据菜单id集合 获取对应的权限
     * @param ids
     * @return
     */
    public List<Menu>getAuthList(@Param("ids") List<Integer> ids);

    /**
     * 获取所有菜单
     * @return
     */
    List<Menu> getMenuList();

    /**
     * 删除菜单关联的角色
     * @return
     */
    Integer deleteRole(Integer id);


    /**
     * 查看角色对应的权限
     * @return
     */
    public List<String> getRoleAuth(@Param("roleId") Integer roleId);

    /**
     * 查询用户关联的菜单id
     * @param userId
     * @return
     */
    public List<Integer> getMenuIdsByUserId(@Param("userId") Integer userId);

    /**
     * 根据菜单id集合查询父级id
     * @param ids
     * @return
     */
    public List<Integer> getPIdsByIds(@Param("ids") List<Integer> ids);
}
