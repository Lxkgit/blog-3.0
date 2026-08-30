package com.blog.file.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.netty.service.NettySyncFileReceiveService;
import com.blog.file.service.CameraService;
import com.blog.redis.constant.AuthRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * @Description 视频授权类服务
 * @Author lxk
 * @CreateTime 2026-08-26
 */

@Service
public class CameraServiceImpl implements CameraService {

    private static final Logger logger = LoggerFactory.getLogger(CameraServiceImpl.class);

    /**
     * Redis Key 前缀
     */
    private static final String TOKEN_PREFIX = "video:token:";

    /**
     * Token 有效时间：10分钟
     * 单位：秒
     */
    private static final long TOKEN_EXPIRE = 10 * 60;

    /**
     * 安全随机数
     */
    private final SecureRandom secureRandom = new SecureRandom();

    @Resource
    private RedisService redisService;


    /**
     * 创建视频临时 Token
     *
     * @param stream 视频流，例如 cam1
     * @return Token 信息
     */
    @Override
    public JSONObject createToken(String stream) {

        Integer userId = SecurityUtil.getLoginUser().getId();

        if (stream == null || stream.isBlank()) {
            throw new IllegalArgumentException("stream不能为空");
        }

        // 生成随机 Token
        String token = generateToken();

        // Redis Key
        String key = TOKEN_PREFIX + token;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        jsonObject.put("userId", userId);
        jsonObject.put("stream", stream);
        jsonObject.put("time", new Date());
        jsonObject.put("expiresIn", TOKEN_EXPIRE);

        // 保存 Redis，10分钟后自动过期
        redisService.setString(key, jsonObject.toJSONString(), TOKEN_EXPIRE);

        return jsonObject;
    }


    /**
     * 验证视频 Token
     *
     * @param token Token
     * @return Token 信息，验证失败返回 null
     */
    @Override
    public JSONObject validateToken(String token) {
        logger.info("收到token： {}", token);
        if (token == null || token.isBlank()) {
            return null;
        }
        String key = TOKEN_PREFIX + token;
        Object value = redisService.getString(key);
        if (value == null) {
            return null;
        }
        String json = value.toString();
        if (json.isBlank()) {
            return null;
        }
        try {
            return JSONObject.parseObject(json);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 主动删除 Token
     *
     * @param token Token
     */
    @Override
    public void revokeToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        redisService.delKey(TOKEN_PREFIX + token);
    }


    /**
     * 刷新 Token 有效期
     * 这里只延长当前 Token 的有效期，
     * 不生成新的 Token。
     *
     * @param token Token
     * @return 是否刷新成功
     */
    @Override
    public boolean refreshToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String key = TOKEN_PREFIX + token;
        // Token 不存在
        if (!redisService.hasKey(key)) {
            return false;
        }
        // 重新设置10分钟有效期
        return redisService.setExpire(key, TOKEN_EXPIRE);
    }


    /**
     * 获取 Token 剩余有效时间
     *
     * @param token Token
     * @return 剩余秒数
     */
    @Override
    public long getExpire(String token) {
        if (token == null || token.isBlank()) {
            return 0;
        }
        Long expire = redisService.getExpire(TOKEN_PREFIX + token);
        if (expire == null || expire < 0) {
            return 0;
        }
        return expire;
    }


    /**
     * 生成安全随机 Token
     * 32字节随机数据
     */
    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}