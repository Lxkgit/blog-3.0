package com.blog.content.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
public class TestController {

    @GetMapping("/token")
    public Object home(@AuthenticationPrincipal Object principal) {
        return principal;
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
