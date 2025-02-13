package org.example.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-02-12
 */

@RestController
public class TokenController {

    @GetMapping("/token")
    public Object home(@AuthenticationPrincipal Object principal) {
        return principal;
    }

}
