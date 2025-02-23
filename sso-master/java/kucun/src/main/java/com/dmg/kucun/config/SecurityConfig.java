package com.dmg.kucun.config;

import com.dmg.kucun.filter.MyAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Autowired
    private MyAuthenticationFilter myAuthenticationFilter;


 
    /**
     *
     * 安全过滤器链
     * @param
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        //禁止csrf
        http.csrf().disable();
        //拦截所有请求
        http.authorizeHttpRequests(x->x.anyRequest().authenticated())
                //先经过自定义的jwt认证过滤器，在处理授权重定向过滤器
                .addFilterBefore(myAuthenticationFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                //禁用默认的oauth2登陆 否则会一直跳到授权路径
                .oauth2Login().disable()
                //使用默认客户端配置
                .oauth2Client(Customizer.withDefaults())
        ;
        return http.build();
    }




}