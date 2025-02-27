package com.blog.auth.controller;

import com.blog.core.entity.auth.vo.Oauth2Vo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.utils.HttpUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSONObject;

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
            return ResultFactory.buildSuccessResult(res);
        }
        return ResultFactory.buildFailResult(res.getString("msg"));
    }




}
