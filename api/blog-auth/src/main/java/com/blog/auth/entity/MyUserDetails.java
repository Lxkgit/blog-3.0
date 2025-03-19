package com.blog.auth.entity;

import com.blog.core.domain.auth.bo.LoginUserBo;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserDetails 用户详细信息接口
 *
 * @param
 * @return
 * @throws Exception
 */
@Data
public class MyUserDetails implements UserDetails {


    private LoginUserBo loginUserBo;

    private List<SimpleGrantedAuthority> simpleGrantedAuthorityList;

    public MyUserDetails(LoginUserBo user, List<SimpleGrantedAuthority> simpleGrantedAuthorityList) {
        this.loginUserBo = user;
        this.simpleGrantedAuthorityList = simpleGrantedAuthorityList;
    }

    /**
     * 获取所有权限
     *
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return simpleGrantedAuthorityList;
    }

    @Override
    public String getPassword() {
        return loginUserBo.getPassword();
    }

    @Override
    public String getUsername() {
        return loginUserBo.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //是否启用 true:启用, false:禁用
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * 二次处理权限
     * 获取字符串集合的权限
     *
     * @return
     */
    public List<String> getAuthList() {
        //转成list集合
        return simpleGrantedAuthorityList.stream().map(SimpleGrantedAuthority::getAuthority).collect(Collectors.toList());
    }
}