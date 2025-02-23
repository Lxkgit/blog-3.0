package com.dmg.authserver.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.dmg.authserver.service.LoginService;
import com.dmg.authserver.utils.Result;
import com.dmg.authserver.vo.LoginVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.token.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    @Autowired
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
            return Result.error("rzId不能为空");
        }
        loginService.tuiChu(rzId);
        return Result.success("退出成功");
    }
}
