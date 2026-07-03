package com.blog.auth.config.oauth.password;


import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.Assert;

import java.util.Map;


/**
 * @Description Password 模式的 Authentication
 * @Author lxk
 * @CreateTime 2026-07-03
 */
public class PasswordAuthenticationToken
        extends OAuth2AuthorizationGrantAuthenticationToken {

    private static final long serialVersionUID = 1L;

    public PasswordAuthenticationToken(Authentication clientPrincipal, @Nullable Map<String, Object> additionalParameters) {
        super(new AuthorizationGrantType(OAuth2ParameterNames.PASSWORD), clientPrincipal, additionalParameters);
        Assert.notNull(clientPrincipal, "clientPrincipal cannot be null");
    }

}
