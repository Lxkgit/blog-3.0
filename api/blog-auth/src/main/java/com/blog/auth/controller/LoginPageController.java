package com.blog.auth.controller;

import com.blog.auth.service.LoginService;
import com.blog.core.domain.auth.vo.LoginVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-07-06
 */

@Controller
public class LoginPageController {

    @Resource
    private LoginService loginService;

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String login() {
        return "login.html";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String register() {
        return "register.html";
    }

    @PostMapping("/register")
    public String register(LoginVo vo) {
        try {
            return loginService.register(vo);
        } catch (Exception e) {
            // 注册失败
            return "redirect:/register?error=register_failed";
        }
    }
}
