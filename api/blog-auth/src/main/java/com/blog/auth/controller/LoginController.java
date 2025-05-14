package com.blog.auth.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.blog.auth.service.LoginService;
import com.blog.core.domain.auth.vo.LoginVo;
import com.blog.core.domain.auth.vo.Oauth2Vo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.redis.constant.AuthRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {


    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Resource
    private RedisService redisService;

    @Resource
    private LoginService loginService;

    @Value("${redirect.callback}")
    private String callbackUrl;

    @Value("${redirect.serviceIp}")
    private String serviceIp;

    /**
     * 登录
     *
     * @param loginVo
     * @return
     */
    @PostMapping("/doLogin")
    public Result doLogin(@RequestBody LoginVo loginVo) throws ServiceException {
        return loginService.login(loginVo);
    }

    /**
     * 拿着授权码,手动获取token
     *
     * @param vo
     * @return
     */
    @PostMapping("/getToken")
    public Result getToken(@RequestBody Oauth2Vo vo) throws ServiceException {
        return ResultFactory.buildSuccessResult(loginService.getToken(vo));
    }

    /**
     * 退出登陆
     */
    @PostMapping("/tuiChu")
    public Result tuiChu(HttpServletRequest request) {
        // 获取当前的会话对象
        String rzId = request.getHeader("rzId");
        if (StringUtils.isEmpty(rzId)) {
            return ResultFactory.buildFailResult("rzId不能为空");
        }
        loginService.tuiChu(rzId);
        return ResultFactory.buildSuccessResult("退出成功");
    }

    /**
     * 获取公钥
     *
     * @return
     */
    @GetMapping("/publicKey")
    public Result publicKey() {
        String publicKey = redisService.getString(AuthRedisConstant.PUBLIC_KEY).toString();
        return ResultFactory.buildSuccessResult(publicKey);
    }

    /**
     * 获取重定向地址
     * @return
     */
    @GetMapping("/redirect")
    private Result redirect() {
        Map<String, String> map = new HashMap<>();
        map.put("callbackUrl", callbackUrl);
        map.put("serviceIp", serviceIp);
        return ResultFactory.buildSuccessResult(map);
    }

    /**
     * 资源服务 获取用户账号
     *
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/getUser")
    public Result getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return ResultFactory.buildSuccessResult(authentication);
    }

    /**
     * 获取用户权限
     *
     * @param
     * @return
     * @throws Exception
     */
    @GetMapping("/getAuth")
    public String getAuth(Authentication authentication) {
        String name = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        log.info("权限信息:{}", authorities);
        return name;
    }

    /**
     * 获取用户认证授权信息
     *
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/getAuthentication")
    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("接口调用 ... ");
        return authentication;
    }


}
