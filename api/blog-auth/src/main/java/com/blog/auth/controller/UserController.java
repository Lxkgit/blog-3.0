package com.blog.auth.controller;

import com.blog.auth.config.oauth.service.AuthService;
import com.blog.auth.service.UserService;
import com.blog.core.domain.auth.vo.UserVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 用户接口
 * @Author lxk
 * @CreateTime 2025-03-24
 */

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping(value = "/select/id")
    public UserVo selectUserById(@RequestParam(value = "userId") Integer userId){
        return userService.selectUserById(userId);
    }
}
