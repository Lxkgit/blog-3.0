package com.blog.content.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public String test() {
        return "content - test";
    }

    @GetMapping("/hello1")
    public String test1() {
        return "test1";
    }
}
