package com.blog.file.config.oauth;

import com.blog.core.constant.PermitUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 安全配置类
 *
 * @param
 * @return
 * @throws Exception
 */
@Configuration
public class SecurityConfig {


    /**
     * 从yml文件获取oauth2 jwt令牌签发者的地址
     */
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    /**
     * 安全过滤器链
     *
     * @param
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //禁止csrf
        http.csrf(AbstractHttpConfigurer::disable);

        //拦截所有请求
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(PermitUrl.permitAllUrl("file")).permitAll()
                .anyRequest().authenticated()
        );
        //oauth2资源服务器 使用jwt 带着jwt的token访问资源服务器
        //使用JWT解码器来验证JWT令牌的签名和内容
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .decoder(JwtDecoders.fromIssuerLocation(issuerUri))
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
        );

        return http.build();
    }

    /**
     * JWT 认证转换器 将令牌转换成身份认证对象
     *
     * @return
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        //从jwt令牌中提取授权信息
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        //jwt的授权信息在auths 参数中
        grantedAuthoritiesConverter.setAuthoritiesClaimName("auths");
        //授权信息不包含前缀
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        //认证转换器
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        //权限转换对象放入
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }


}