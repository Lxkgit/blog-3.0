package com.blog.auth.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.auth.mapper.UserMapper;
import com.blog.auth.service.LoginService;
import com.blog.core.constant.Constant;
import com.blog.core.domain.auth.entity.User;
import com.blog.core.domain.auth.vo.LoginVo;
import com.blog.core.domain.auth.vo.Oauth2Vo;
import com.blog.core.exception.ServiceException;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.utils.HttpUtils;
import com.blog.core.utils.RSAUtil;
import com.blog.redis.constant.AuthRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {


    //认证管理器
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private RedisService redisService;

    @Resource
    private UserMapper userMapper;

    /**
     * 登陆
     *
     * @param vo
     * @return
     */
    @Override
    public Result login(LoginVo vo) throws ServiceException {
        return ResultFactory.buildSuccessResult(getRzId(vo.getUsername(), vo.getPassword()));
    }

    /**
     * 退出
     *
     * @param rzId
     */
    @Override
    public void tuiChu(String rzId) {
        String key = AuthRedisConstant.RZ_ID + ":" + rzId;
        //清空上下文
        SecurityContextHolder.clearContext();
        //删除缓存
        redisService.delKey(key);
    }

    @Override
    public JSONObject getToken(Oauth2Vo vo) throws ServiceException {
        //拼接获取token的路径
        String url = "http://auth-server:60001/auth/oauth2/token";
        Map<String, String> map = new HashMap<>();

        String rzId = "";
        if ("authorization_code".equals(vo.getGrantType())) {
            map.put("code", vo.getCode());
            map.put("client_id", vo.getClientId());
            map.put("redirect_uri", vo.getRedirectUri());
            map.put("grant_type", vo.getGrantType());
        } else if ("refresh_token".equals(vo.getGrantType())) {
            map.put("client_id", vo.getClientId());
            map.put("grant_type", vo.getGrantType());
            map.put("refresh_token", vo.getRefreshToken());
            map.put("client_secret", vo.getClientSecret());
            rzId = getRzId(vo.getUsername(), vo.getPassword());
        }
        JSONObject jsonObject = HttpUtils.doPost(url, map, vo);
        if (jsonObject != null) {
            jsonObject.put("rz_id", rzId);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, vo.getUsername());
            User user = userMapper.selectOne(wrapper);
            if (user != null) {
                jsonObject.put("user_id", user.getId());
            }
        }
        return jsonObject;
    }

    private String getRzId(String username, String encryptedPassword) throws ServiceException {

        String privateKey = redisService.getString(AuthRedisConstant.PRIVATE_KEY).toString();
        String password = RSAUtil.decrypt(encryptedPassword, privateKey);

        //根据账号和密码 创建 认证令牌对象
        UsernamePasswordAuthenticationToken upt = new UsernamePasswordAuthenticationToken(username, password);
        //进行登录 获取认证信息
        Authentication authenticate = authenticationManager.authenticate(upt);
        if (authenticate == null) {
            throw new ServiceException("登录失败");
        }
        //认证Id
        String rzId = UUID.randomUUID().toString();
        String key = AuthRedisConstant.RZ_ID + ":" + rzId;
        //创建安全上下文
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        //把用户认证信息放到 安全上下文中
        securityContext.setAuthentication(authenticate);
        //把上下文放到 持有人手中
        SecurityContextHolder.setContext(securityContext);

        // 保存认证信息 过期时间1个小时 保持和access_token的过期时间一致
        redisService.setString(key, securityContext, Constant.AUTH_EFFECTIVE_TIME);
        return rzId;
    }
}
