package com.blog.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.blog.auth.service.LoginService;
import com.blog.core.entity.auth.vo.LoginVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Resource
    private LoginService loginService;


    /**
     * 登录
     * @param loginVo
     * @return
     */
    @PostMapping("/doLogin")
    public Result doLogin(@RequestBody LoginVo loginVo, HttpServletRequest request) {
        return loginService.login(loginVo,request);
    }

    /**
     * 退出登陆
     */
    @PostMapping("/tuiChu")
    public Result tuiChu(HttpServletRequest request){
        // 获取当前的会话对象
        String rzId=request.getHeader("rzId");
        if(StringUtils.isEmpty(rzId)){
            return ResultFactory.buildFailResult("rzId不能为空");
        }
        loginService.tuiChu(rzId);
        return ResultFactory.buildSuccessResult("退出成功");
    }
}
