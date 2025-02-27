package com.blog.core.entity.auth.vo;

import lombok.Data;

@Data
public class Oauth2Vo {

    //回调地址
    private String redirectUri;

    //授权码
    private String code;

    //客户端id
    private String clientId;

    //客户端密码
    private String clientSecret;


}
