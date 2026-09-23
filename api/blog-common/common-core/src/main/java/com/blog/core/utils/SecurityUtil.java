package com.blog.core.utils;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.auth.bo.LoginUserBo;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 27992
 */
public class SecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    @Getter
    private static LoginUserBo loginUser;

    public static void getLoginUserByJwt(String token) {
        // JWT 解析
        try {
            if (StringUtils.isNotEmpty(token) && token.startsWith("Bearer ")) {
                JSONObject jwt = JwtUtil.decodeJwt(token.substring(7));
                SecurityUtil.setLoginUser(jwt);
            } else {
                clearLoginUser();
            }
        } catch (Exception e) {
            logger.error("用户鉴权信息解析异常:{}", e.getMessage(), e);
        }
    }

    public static void setLoginUser(JSONObject jwt) {
        LoginUserBo loginUserBo = new LoginUserBo();
        loginUserBo.setId(jwt.getInteger("id"));
        loginUserBo.setUsername(jwt.getString("username"));
        SecurityUtil.loginUser = loginUserBo;
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

    /**
     * 假登陆，用户
     */
    public static void setUser(Integer id, String username) {
        LoginUserBo loginUserBo = new LoginUserBo();
        loginUserBo.setId(id);
        loginUserBo.setUsername(username);
        SecurityUtil.loginUser = loginUserBo;
    }

    private static void clearLoginUser() {
        SecurityUtil.loginUser = null;
    }

}
