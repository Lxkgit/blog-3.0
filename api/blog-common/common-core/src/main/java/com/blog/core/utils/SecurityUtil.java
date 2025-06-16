package com.blog.core.utils;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.auth.bo.LoginUserBo;
import lombok.Getter;

public class SecurityUtil {

    @Getter
    private static LoginUserBo loginUser;

    @Getter
    private static String rzId;

    public static void setLoginUser(JSONObject jwt) {
        LoginUserBo loginUserBo = new LoginUserBo();
        loginUserBo.setId(jwt.getInteger("id"));
        loginUserBo.setUsername(jwt.getString("username"));
        SecurityUtil.loginUser = loginUserBo;
    }

    public static void setRzId(String rzId) {
        SecurityUtil.rzId = rzId;
    }

    /**
     * 假登陆，用于服务间调用生成创建用户与创建人名称
     */
    public static void setSystem() {
        LoginUserBo loginUserBo = new LoginUserBo();
        loginUserBo.setId(0);
        loginUserBo.setUsername("system");
        SecurityUtil.loginUser = loginUserBo;
    }
}
