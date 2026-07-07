package com.blog.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-07-06
 */

@Controller
public class LoginPageController {

    @GetMapping("/login")
    public String login() {
        return "login.html";
    }

}
