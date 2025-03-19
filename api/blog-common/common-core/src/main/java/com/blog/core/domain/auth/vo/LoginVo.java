package com.blog.core.domain.auth.vo;

import lombok.Data;

@Data
public class LoginVo {

    //用户账号
    private String username;
    //用户密码
    private String password;
    //客户端id
    private String clientId;

    //回调地址
    private String target;

    //全局唯一id 认证id
    private String rzId;
}
