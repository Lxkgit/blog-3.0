package com.dmg.kucun.service;

import org.springframework.security.core.Authentication;

/**
 * 访问远程资源服务接口 前后分离使用
 */
public interface RsService {

    /**
     * 获取资源服务器的认证授权信息
     * @param token 令牌
     * @return
     * @throws Exception
     */
    public Authentication getAuthentication(String token);

}
