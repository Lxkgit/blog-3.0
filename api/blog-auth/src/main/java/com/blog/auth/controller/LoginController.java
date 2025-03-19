package com.blog.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.blog.auth.service.LoginService;
import com.blog.core.domain.auth.vo.LoginVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RestController
public class LoginController {

    @Resource
    private LoginService loginService;

    /**
     * 资源服务 获取用户账号
     *
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/getUser")
    public Result getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return ResultFactory.buildSuccessResult(authentication);
    }

    /**
     * 获取用户权限
     *
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/getAuth")
    public String getAuth(Authentication authentication) {
        String name = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        log.info("权限信息:{}", authorities);
        return name;
    }

    /**
     * 获取用户认证授权信息
     *
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/getAuthentication")
    public Authentication getAuthentication(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("接口调用 ... ");
        return authentication;
    }

    /**
     * 登录
     *
     * @param loginVo
     * @return
     */
    @PostMapping("/doLogin")
    public Result doLogin(@RequestBody LoginVo loginVo, HttpServletRequest request) {
        return loginService.login(loginVo, request);
    }

    /**
     * 退出登陆
     */
    @PostMapping("/tuiChu")
    public Result tuiChu(HttpServletRequest request) {
        // 获取当前的会话对象
        String rzId = request.getHeader("rzId");
        if (StringUtils.isEmpty(rzId)) {
            return ResultFactory.buildFailResult("rzId不能为空");
        }
        loginService.tuiChu(rzId);
        return ResultFactory.buildSuccessResult("退出成功");
    }
}
