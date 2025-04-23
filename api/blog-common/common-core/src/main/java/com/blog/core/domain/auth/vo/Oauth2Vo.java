package com.blog.core.domain.auth.vo;

import lombok.Data;

@Data
public class Oauth2Vo {

    /**
     * 回调地址
     */
    private String redirectUri;

    /**
     * 授权码
     */
    private String code;

    /**
     * 客户端id
     */
    private String clientId;

    /**
     * 客户端密码
     */
    private String clientSecret;

    /**
     * 客户端密码
     */
    private String grantType;

    /**
     * 客户端密码
     */
    private String refreshToken;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
