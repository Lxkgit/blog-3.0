package com.blog.core.utils;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.auth.bo.LoginUserBo;
import lombok.Getter;

public class SecurityUtil {

    @Getter
    private static LoginUserBo loginUserBo;

    public static void setLoginUserBo(JSONObject jwt) {
        LoginUserBo loginUserBo = new LoginUserBo();
        loginUserBo.setId(jwt.getInteger("id"));
        loginUserBo.setUsername(jwt.getString("username"));
        SecurityUtil.loginUserBo = loginUserBo;
    }
}
