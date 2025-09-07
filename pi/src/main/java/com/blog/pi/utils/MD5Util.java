package com.blog.pi.utils;

import com.blog.pi.mqtt.MqttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    private static final Logger logger = LoggerFactory.getLogger(MD5Util.class);

    /**
     * 计算文件的MD5值
     * @param file 要计算MD5的文件
     * @return 文件的MD5值，计算失败时返回null
     */
    public static String getFileMd5(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }

        MessageDigest md5 = null;
        FileInputStream fis = null;

        try {
            md5 = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);

            byte[] buffer = new byte[8192];
            int length;

            // 分块读取文件并更新MD5摘要
            while ((length = fis.read(buffer)) != -1) {
                md5.update(buffer, 0, length);
            }

            // 将字节数组转换为十六进制字符串
            return bytesToHex(md5.digest());

        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("文件MD5值计算失败,文件名称:{}, 错误信息:{}", file.getName(), e.getMessage(), e);
            return null;
        } finally {
            // 确保输入流关闭
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error("输入流关闭异常:{}", e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            // 转换为十六进制
            String hex = Integer.toHexString(0xFF & b);
            // 保证每个字节用两个字符表示
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}