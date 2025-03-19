package com.blog.core.utils;

import com.alibaba.fastjson2.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JwtUtil {

    public static JSONObject decodeJwt(String jwt) {

        // jwt
        String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT");
        }

        String header = parts[0];
        String payload = parts[1];
        String signature = parts[2];

        // Base64Url 解码
        byte[] payloadBytes = Base64.getDecoder().decode(payload);
        String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);

        return JSONObject.parseObject(payloadJson);
    }
}
