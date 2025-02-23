package com.dmg.authserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.dmg.authserver.utils.HttpUtils;
import com.dmg.authserver.utils.Result;
import com.dmg.authserver.vo.LoginVo;
import com.dmg.authserver.vo.Oauth2Vo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {


    /**
     * 拿着授权码,手动获取token
     * @param vo
     * @return
     */
    @PostMapping("/getToken")
    public Result getToken(@RequestBody Oauth2Vo vo){
        //http://auth-server:8084/oauth2/token?redirect_uri=http://localhost:5173/user&grant_type=authorization_code&code=
        //拼接获取token的路径
        String url="http://auth-server:8084/oauth2/token?" +
                "redirect_uri="+vo.getRedirectUri()+"&grant_type=authorization_code&code="+vo.getCode();
        Map<String,String> map=new HashMap<>();
        JSONObject res = HttpUtils.doPost(url, map,vo);
        if(res!=null && res.getInteger("statusCode")==200){
            return Result.success(res);
        }
        return Result.error(res.getString("msg"));
    }




}
