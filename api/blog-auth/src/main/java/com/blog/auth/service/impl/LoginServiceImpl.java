package com.blog.auth.service.impl;


import com.alibaba.fastjson2.JSON;
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
import com.blog.mq.entity.MqMessage;
import com.blog.mq.enums.MqMsgEnum;
import com.blog.mq.enums.MqTopicEnum;
import com.blog.mq.service.MQProducerService;
import com.blog.redis.constant.AuthRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author: lxk
 * @date 2026年8月14日
 * @description: 登录服务类
 */

@Service
public class LoginServiceImpl implements LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private RedisService redisService;

    @Resource
    private UserMapper userMapper;


    @Resource
    private MQProducerService mqProducerService;

    @Value("${auth.issuer}")
    private String issuer;

    @Override
    public String register(LoginVo vo) throws ServiceException {

        if (StringUtils.isBlank(vo.getUsername())) {
            throw new ServiceException("用户名不能为空");
        }

        if (StringUtils.isBlank(vo.getPassword())) {
            throw new ServiceException("密码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, vo.getUsername());

        User existUser = userMapper.selectOne(wrapper);

        if (existUser != null) {
            return "redirect:/register?error=user_exist";
        }

        User user = new User();
        user.setUsername(vo.getUsername());
        user.setPassword(passwordEncoder.encode(vo.getPassword()));

        userMapper.insert(user);
        userMapper.insertUserRoles(user.getId(), List.of(2));

        MqMessage mqMessage = new MqMessage(MqTopicEnum.BLOG_SYSTEM_DATA_REGISTER, MqMsgEnum.ADD.getType(),
                JSON.toJSONString(user), User.class);
        mqProducerService.sendSyncOrderly(mqMessage);

        return "redirect:/login?success=register";
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
        logger.info("获取 OAuth2 token，grantType={}", vo.getGrantType());

        if (vo.getGrantType() == null) {
            throw new ServiceException("grantType不能为空");
        }

        // 目前只实现 authorization_code
        if (!"authorization_code".equals(vo.getGrantType())) {
            throw new ServiceException("暂不支持的授权类型：" + vo.getGrantType());
        }

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", vo.getGrantType());
        params.put("code", vo.getCode());
        params.put("client_id", vo.getClientId());
        params.put("redirect_uri", vo.getRedirectUri());

        // 直接调用 auth 服务，不再经过 gateway
        String url = issuer + "/oauth2/token";

        JSONObject jsonObject = HttpUtils.doPost(url, params, vo);

        if (jsonObject == null) {
            throw new ServiceException("获取 token 失败");
        }

        // 查询用户信息
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, vo.getUsername());

        User user = userMapper.selectOne(wrapper);

        if (user != null) {
            jsonObject.put("user_id", user.getId());
        }

        return jsonObject;
    }
}
