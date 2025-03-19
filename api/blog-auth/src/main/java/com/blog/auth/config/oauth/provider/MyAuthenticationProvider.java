package com.blog.auth.config.oauth.provider;

import com.blog.auth.config.oauth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 实现身份验证提供程序
 *
 * @param
 * @return
 * @throws Exception
 */
@Slf4j
@Component
public class MyAuthenticationProvider implements AuthenticationProvider {

    @Resource
    private UserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;


    /**
     * LoginServiceImpl的登录方法点击认证的时候 直接跳转到这里
     * 登陆认证
     *
     * @param
     * @return
     * @throws Exception
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //从authentication获取用户名和凭证(密码)信息
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        log.info("密码=========================={}", password);
        //查询用户是否存在
        UserDetails userDetails = userService.loadUserByUsername(username);
        //比较和数据库的密码是否一样
        if (passwordEncoder.matches(password, userDetails.getPassword())) {
            //返回用户名密码认证令牌
            //因为UsernamePasswordAuthenticationToken的上级父类的父类是Authentication 所以可以直接返回
            return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
        } else {
            throw new BadCredentialsException("用户名或者密码错误了");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        //保证认证和返回的对象都是UsernamePasswordAuthenticationToken
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}