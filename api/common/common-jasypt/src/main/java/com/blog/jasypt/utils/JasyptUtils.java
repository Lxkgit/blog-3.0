package com.blog.jasypt.utils;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.util.HashMap;
import java.util.Map;

public class JasyptUtils {

    private static final Map<String, String> map = new HashMap<>();

    static {
        map.put("mysql          账号: root        密码: ", "MySql@Admin123*.");
        map.put("redis                           密码: ", "redis-960@*");
        map.put("elasticsearch  账号: elastic     密码: ", "elasticsearch-960@*");
        map.put("minio          账号: minio       密码: ", "minio-960@*");
        map.put("ftp            账号: system      密码: ", "Ftp@Admin123*.");
    }


    /**
     * 对 message 消息 加解密
     *
     * @param secretKey ：密钥。加/解密必须使用同一个密钥
     * @param message   ：加/解密的内容
     * @param isEncrypt ：true 表示加密、false 表示解密
     * @return 加/解密后的数据
     */
    public static String stringEncryptor(String secretKey, String message, boolean isEncrypt) {
        PooledPBEStringEncryptor pooledPBEStringEncryptor = new PooledPBEStringEncryptor();
        pooledPBEStringEncryptor.setConfig(getSimpleStringPBEConfig(secretKey));
        return isEncrypt ? pooledPBEStringEncryptor.encrypt(message) : pooledPBEStringEncryptor.decrypt(message);
    }
    /**
     * 设置  PBEConfig 配置对象，SimpleStringPBEConfig 是它的实现类
     * 1、所有的配置项建议与全局配置文件中的配置项保持一致，特别是 password、algorithm 等等选项，如果不一致，则应用启动时解密失败而报错.
     * 2、setPassword(final String password)：设置加密密钥，必须与全局配置文件中配置的保存一致，否则应用启动时会解密失败而报错.
     * 3、setPoolSize(final String poolSize)：设置要创建的加密程序池的大小.
     * 4、setAlgorithm(final String algorithm): 设置加密算法的值， 此算法必须由 JCE 提供程序支持
     * 5、setKeyObtentionIterations: 设置应用于获取加密密钥的哈希迭代次数。
     * 6、setProviderName(final String providerName)：设置要请求加密算法的安全提供程序的名称
     * 7、setSaltGeneratorClassName：设置 Sal 发生器
     * 8、setIvGeneratorClassName：设置 IV 发生器
     * 9、setStringOutputType：设置字符串输出的编码形式。可用的编码类型有 base64、hexadecimal
     *
     * @param secretKey 密钥。加/解密必须使用同一个密钥
     * @return 配置信息
     */
    private static SimpleStringPBEConfig getSimpleStringPBEConfig(String secretKey) {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(secretKey);
        config.setPoolSize("1");
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        return config;
    }

    public static void main(String[] args) throws Exception {

        String secretKey = "gszero";

        map.forEach((key, value) -> {
            System.out.println(key + stringEncryptor(secretKey, value, true));
        });
    }
}
