package com.blog.core.utils;

import cn.hutool.core.codec.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class RSAUtil {

    private static final Logger log = LoggerFactory.getLogger(RSAUtil.class);

    public static String decrypt(String value, String privateKey) {
        try {
            if (StringUtils.isEmpty(value)) {
                return "";
            }
            byte[] data;
            if (value.length() != 256) {
                data = Base64.decode(value);
            } else {
                data = value.getBytes();
            }
            byte[] decryptByPrivateKey = decryptByPrivateKey(data, Base64.decode(privateKey));
            return new String(decryptByPrivateKey, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.info("RSAUtil decrypt error: {}", e.getMessage(), e);
        }
        return "";
    }

    /**
     * 私钥解密
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return byte[] 解密数据
     */
    private static byte[] decryptByPrivateKey(byte[] data, byte[] key) throws Exception {
        //取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //生成私钥
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        //数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

}
