package com.blog.auth.service.impl;


import com.blog.core.constant.RedisConstant;
import com.blog.auth.service.LoginService;
import com.blog.core.domain.auth.vo.LoginVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {


    //认证管理器
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private RedisService redisService;

    /**
     * 登陆
     *
     * @param vo
     * @return
     */
    @Override
    public Result login(LoginVo vo) {
        //根据账号和密码 创建 认证令牌对象
        UsernamePasswordAuthenticationToken upt = new UsernamePasswordAuthenticationToken(vo.getUsername(), vo.getPassword());
        //进行登录 获取认证信息
        Authentication authenticate = authenticationManager.authenticate(upt);
        if (authenticate == null) {
            throw new RuntimeException("登录失败");
        }
        //认证Id
        String rzId = UUID.randomUUID().toString();
        String key = RedisConstant.RZ_ID + ":" + rzId;
        //创建安全上下文
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        //把用户认证信息放到 安全上下文中
        securityContext.setAuthentication(authenticate);
        //把上下文放到 持有人手中
        SecurityContextHolder.setContext(securityContext);

        // 保存认证信息 过期时间1个小时 保持和access_token的过期时间一致
        redisService.setString(key, securityContext, 3600);
        return ResultFactory.buildSuccessResult(rzId);
    }

    /**
     * 退出
     *
     * @param rzId
     */
    @Override
    public void tuiChu(String rzId) {
        String key = RedisConstant.RZ_ID + ":" + rzId;
        //清空上下文
        SecurityContextHolder.clearContext();
        //删除缓存
        redisService.delKey(key);
    }
}
