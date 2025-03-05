package com.blog.auth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.auth.config.filter.MyAuthenticationFilter;
import com.blog.auth.config.point.MyLoginUrlAuthenticationEntryPoint;
import com.blog.auth.config.repository.RedisSecurityContextRepository;
import com.blog.auth.dao.UserMapper;
import com.blog.auth.entity.MyUserDetails;
import com.blog.core.constant.PermitUrl;
import com.blog.core.entity.auth.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.List;

import com.fasterxml.jackson.databind.Module;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

//开启web安全 应用在web环境下
// 1: 加载了WebSecurityConfiguration配置类, 配置安全认证策略
// 2: 加载了AuthenticationConfiguration, 配置了认证信息
@Slf4j
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisSecurityContextRepository redisSecurityContextRepository;

    @Resource
    private MyAuthenticationFilter myAuthenticationFilter;

    //密码加密
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 授权服务安全过滤器链
     * 第一个进来
     *
     * @param
     * @return
     * @throws Exception
     */
    @Order(1)
    @Bean
    public SecurityFilterChain authFilterChain(HttpSecurity http) throws Exception {
        log.info("第一个过滤器");

        //授权服务配置 应用默认安全性 简化配置,在源码给你都配置好了
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        //禁用session,前后端分离不需要, cookie中就不会显示JSESSIONID
        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        //配置上下文 从redis中读取
        http.securityContext(x -> x.securityContextRepository(redisSecurityContextRepository));
        //配置OpenID Connect（OIDC）登录,是一种在OAuth 2.0基础上实现身份验证和授权的协议。
        //与传统的OAuth 2.0授权不同的是，OIDC需要在OAuth 2.0授权服务器和OAuth客户端之间建立信任关系，
        // 并使用JWT（JSON Web Tokens）来安全地传输信息
        //生成oidc授权码和令牌 在客户端使用scope:openid 的时候就会生效 返回对应的授权码
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).oidc(Customizer.withDefaults());
        //异常处理
        http.exceptionHandling(x -> x.defaultAuthenticationEntryPointFor(
                //自定义未登录地址,地址为前端vue的地址，当没有登陆的时候，自动跳转到前端登陆界面
                new MyLoginUrlAuthenticationEntryPoint("http://localhost:3001/denglu"),
                //只有带有 "text/html" 媒体类型的请求需要进行身份验证
                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
        ));
        //资源服务器通过jwt令牌 去访问
        http.oauth2ResourceServer(x -> x.jwt(Customizer.withDefaults()));
        //禁用csrf
        http.csrf(AbstractHttpConfigurer::disable);
        //建造对象
        return http.build();
    }

    //忽略路径 放行路径
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        //放行登录接口 这样才能登录成功
        return x -> x.ignoring().requestMatchers("/doLogin", "/getToken", "/login");
    }

    /**
     * 默认安全过滤器链
     * 用于身份认证
     * 第二个进入
     *
     * @param
     * @return
     * @throws Exception
     */
    @Order(2)
    @Bean
    public SecurityFilterChain appFilterChain(HttpSecurity http) throws Exception {
        log.info("第二个过滤器");
        //先进行自定义的过滤器,在进行账号密码验证
        http.addFilterBefore(myAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http
                .securityMatcher("/auth/**")
                .authorizeHttpRequests((authorize) -> authorize
                        //放行资源
                        .requestMatchers("/auth/doLogin", "/auth/login", "/auth/getToken").permitAll()
                        .requestMatchers("/auth/oauth2/authorize").authenticated()
                        .requestMatchers(PermitUrl.permitAllUrl("auth")).permitAll()
                        .anyRequest().authenticated()
                )
                //禁用表单登陆 前后分离不在使用
                .formLogin(AbstractHttpConfigurer::disable);
        //禁用csrf
//        http.csrf(AbstractHttpConfigurer::disable);
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/auth/oauth2/token"));
        return http.build();
    }

    /**
     * 用于第三方认证
     * 主要管理第三方的客户端
     * 已注册客户端存储库
     *
     * @param
     * @return
     * @throws Exception
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        //从数据库读取注册的客户端信息
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * 解码JWT，并验证其签名
     * 在别的客户端会通过issuerUri这个路径来进行认证(登录)
     *
     * @param jwkSource
     * @return
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * 通过非对称加密生成access_token(jwt)的签名部分
     *
     * @param
     * @return
     * @throws Exception
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * 生成秘钥对,为jwkSource提供服务,私钥服务器自身持有,公钥对外开放。
     *
     * @return
     */
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }


    /**
     * 授权服务设置
     *
     * @return
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                // 关键：设置 issuer 包含上下文路径
                .issuer("http://auth-server:60002/auth")
                // 端点路径不需要包含 /auth，Spring 会自动附加 context-path
                .authorizationEndpoint("/oauth2/authorize")
                .tokenEndpoint("/oauth2/token")
                .tokenIntrospectionEndpoint("/oauth2/introspect")
                .tokenRevocationEndpoint("/oauth2/revoke")
                .jwkSetEndpoint("/oauth2/jwks")
                .oidcUserInfoEndpoint("/userinfo")
                .oidcClientRegistrationEndpoint("/connect/register")
                .build();
    }

    /**
     * 对应 oauth2_authorization表 授权服务
     *
     * @param
     * @return
     * @throws Exception
     */
    @Bean
    public OAuth2AuthorizationService auth2AuthorizationService() {
        //解决自定义user 登录报错
        JdbcOAuth2AuthorizationService service = new JdbcOAuth2AuthorizationService(jdbcTemplate,
                registeredClientRepository());
        JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper authorizationRowMapper =
                new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(
                        registeredClientRepository());
        authorizationRowMapper.setLobHandler(new DefaultLobHandler());

        ObjectMapper objectMapper = new ObjectMapper();
        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        objectMapper.registerModules(securityModules);
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        //放入自定义的user类
        objectMapper.addMixIn(MyUserDetails.class, MyUserMixin.class);
        authorizationRowMapper.setObjectMapper(objectMapper);

        service.setAuthorizationRowMapper(authorizationRowMapper);
        return service;
    }

    /**
     * 对应oauth2_authorization_consent表
     * 用户确认授权同意书
     *
     * @param
     * @return
     * @throws Exception
     */
    @Bean
    public OAuth2AuthorizationConsentService auth2AuthorizationConsentService() {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository());
    }


    /**
     * jwt编码上下文oauth2令牌自定义程序
     * 给jwt 添加一些自定义的参数
     *
     * @param
     * @return
     * @throws Exception
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            JwtClaimsSet.Builder claims = context.getClaims();
            //获取原有的jwt 参数
            Map<String, Object> map = claims.build().getClaims();
            log.info("==========={}", map);
            //获取账号
            String sub = map.get("sub").toString();
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, sub);
            //根据账号获取用户信息
            User user = userMapper.selectOne(wrapper);

            //获得认证对象,当前用户信息
            Authentication principal = context.getPrincipal();
            if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
                //如果jwt的类型是access_token
                List<String> auths = new ArrayList<>();
                //得到该用户的权限信息 放入集合
                for (GrantedAuthority authority : principal.getAuthorities()) {
                    auths.add(authority.getAuthority());
                }
                //写入jwt
                context.getClaims().claim("auths", auths);
                context.getClaims().claim("name", user.getNickname());
                context.getClaims().claim("email", "aaa@qq.com");
                context.getClaims().claim("phone", "12345678901");
            }
            if (context.getTokenType().getValue().equals(OidcParameterNames.ID_TOKEN)) {
                //如果jwt的类型是id_token
                List<String> auths = new ArrayList<>();
                //得到该用户的权限信息 放入集合
                for (GrantedAuthority authority : principal.getAuthorities()) {
                    auths.add(authority.getAuthority());
                }
                //写入jwt
                context.getClaims().claim("auths", auths);
                context.getClaims().claim("name", user.getNickname());
                context.getClaims().claim("email", "abc@qq.com");
                context.getClaims().claim("phone", "12345678902");
            }
        };
    }


    /**
     * 把认证管理器注入到容器
     * LoginServiceImpl类中 才能使用这个认证接口
     *
     * @param config
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


}