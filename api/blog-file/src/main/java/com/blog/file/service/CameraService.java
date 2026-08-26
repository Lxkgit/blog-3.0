package com.blog.file.service;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.utils.SecurityUtil;

import java.util.Date;

/**
 * @author lxk
 * @description
 * @date 2026/08/26
 */

public interface CameraService {


    JSONObject createToken(String stream);


    /**
     * 验证视频 Token
     *
     * @param token  Token
     * @return Token 信息，验证失败返回 null
     */
    JSONObject validateToken(String token);


    /**
     * 主动删除 Token
     *
     * @param token Token
     */
    void revokeToken(String token);


    /**
     * 刷新 Token 有效期
     * 这里只延长当前 Token 的有效期，
     * 不生成新的 Token。
     *
     * @param token Token
     * @return 是否刷新成功
     */
    boolean refreshToken(String token);


    /**
     * 获取 Token 剩余有效时间
     *
     * @param token Token
     * @return 剩余秒数
     */
    long getExpire(String token);
}
