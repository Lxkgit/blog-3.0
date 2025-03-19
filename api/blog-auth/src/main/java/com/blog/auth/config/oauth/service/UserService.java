package com.blog.auth.config.oauth.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.blog.auth.dao.MenuMapper;
import com.blog.auth.dao.UserMapper;
import com.blog.core.domain.auth.bo.LoginUserBo;
import com.blog.core.domain.auth.entity.Menu;
import com.blog.auth.entity.MyUserDetails;
import com.blog.core.domain.auth.entity.User;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * UserDetailsService 用户查询接口
 *
 * @param
 * @return
 * @throws Exception
 */
@Slf4j
@Service
public class UserService implements UserDetailsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private MenuMapper menuMapper;


    /**
     * 根据账号查询用户信息
     *
     * @param
     * @return
     * @throws Exception
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("根据账号查询用户信息==========================================================");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        LoginUserBo loginUserBo = new LoginUserBo();
        BeanUtil.copyProperties(user, loginUserBo);
        if (user == null) {
            log.info("用户不存在");
            throw new UsernameNotFoundException("用户不存在");
        }
        //根据用户id获取权限信息
        List<Menu> auths = getAuthList(user.getId());
        //组装权限信息 放入 SimpleGrantedAuthority
        List<SimpleGrantedAuthority> simpleGrantedAuthorityList = getGrantedAuthority(auths);
        //把权限放入用户对象中
        loginUserBo.setMenu(auths);
        //最后返回UserDetails对象
        return new MyUserDetails(loginUserBo, simpleGrantedAuthorityList);
    }


    /**
     * 组装权限信息 放入 SimpleGrantedAuthority
     *
     * @return
     */
    private List<SimpleGrantedAuthority> getGrantedAuthority(List<Menu> auths) {
        List<SimpleGrantedAuthority> list = new ArrayList<>();
        for (Menu x : auths) {
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(x.getAuth());
            list.add(simpleGrantedAuthority);
        }
        return list;
    }


    /**
     * 根据用户id 获取所有权限
     * 在分配权限的时候 如果只选择了按钮
     * 那么也会把他的父级菜单给查询出来
     * <p>
     * 只有按钮都不选择的时候 菜单才不展示
     *
     * @param userId
     * @return
     */
    private List<Menu> getAuthList(Integer userId) {
        //查询用户关联的菜单id  按钮
        List<Integer> menuIds = menuMapper.getMenuIdsByUserId(userId);
        if (CollectionUtils.isEmpty(menuIds)) {
            //如果还没有授权 直接返回空
            return new ArrayList<>();
        }
        //根据菜单id 查询父级 id  二级菜单
        List<Integer> pIds = menuMapper.getPIdsByIds(menuIds);
        if (CollectionUtils.isEmpty(pIds)) {
            //如果没有父级id 直接返回关联的权限
            return menuMapper.getAuthList(menuIds);
        }
        //根据pIds 查询父级 id  一级菜单
        List<Integer> oneIds = menuMapper.getPIdsByIds(pIds);
        //通过所有的菜单id 查询权限信息
        oneIds.addAll(pIds);
        oneIds.addAll(menuIds);
        return menuMapper.getAuthList(oneIds);
    }
}