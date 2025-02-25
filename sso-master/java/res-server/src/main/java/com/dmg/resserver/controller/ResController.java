package com.dmg.resserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.dmg.resserver.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class ResController {



    @GetMapping("/hello")
    public String hello(){
        return "我是资源服务器hello";
    }

    /**
     * 资源服务 获取用户账号
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/getUser")
    public Result getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Result.success(authentication.getName());
    }


    /**
     * 获取用户权限
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/getAuth")
    public String getAuth(Authentication authentication){
        String name=authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        log.info("权限信息:{}", authorities);
        return name;
    }

    /**
     * 获取用户认证授权信息
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/getAuthentication")
    public Authentication getAuthentication(HttpServletRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("接口调用 ... ");
        return authentication;
    }
}
