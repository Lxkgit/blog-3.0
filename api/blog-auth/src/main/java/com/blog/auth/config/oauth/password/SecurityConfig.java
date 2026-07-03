//package com.blog.auth.config.oauth.password;
//
//import jakarta.annotation.Resource;
//import lombok.extern.log4j.Log4j2;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.MediaType;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
//import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
//import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
//import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
//import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
//
///**
// * @Description
// * @Author lxk
// * @CreateTime 2026-07-03
// */
//
//@Configuration
//@EnableWebSecurity
//@Log4j2
//public class SecurityConfig {
//
//    @Resource
//    private CustomUserDetailsService customUserDetailsService;
//
//    @Bean
//    @Order(1)
//    public SecurityFilterChain authorizationServerSecurityFilterChain(
//            HttpSecurity http,
//            OAuth2AuthorizationService authorizationService,
//            RegisteredClientRepository registeredClientRepository,
//            OAuth2TokenGenerator<?> tokenGenerator,
//            PasswordEncoder passwordEncoder
//    ) throws Exception {
//        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
//                OAuth2AuthorizationServerConfigurer.authorizationServer();
//
//        http
//                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
//                .with(authorizationServerConfigurer, (authorizationServer) ->
//                        authorizationServer
//                                .oidc(Customizer.withDefaults())
//                )
//                .authorizeHttpRequests((authorize) ->
//                        authorize
//                                .anyRequest().authenticated()
//                )
//                .exceptionHandling((exceptions) -> exceptions
//                        .defaultAuthenticationEntryPointFor(
//                                new LoginUrlAuthenticationEntryPoint("/login"),
//                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
//                        )
//                );
//        // 设置自定义 UserDetailsService
//        http.userDetailsService(customUserDetailsService);
//        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
//                .tokenEndpoint(tokenEndpoint ->
//                        tokenEndpoint
//                                .accessTokenRequestConverter(new PasswordAuthenticationConverter())
//                                .authenticationProvider(new PasswordAuthenticationProvider(authorizationService, tokenGenerator)));
//        return http.build();
//    }
//
//}
