package com.blog.file.util;

import com.alibaba.fastjson2.JSONObject;
import com.blog.mq.config.RocketMQConfig;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-10-13
 */

public class VideoUtil {

    private static final Logger logger = LoggerFactory.getLogger(VideoUtil.class);

    public static String resolveVideo(MultipartFile file) {
        if (!isVideoFile(file)) {
            // 不是视频类型文件
            return null;
        }
        JSONObject result = new JSONObject();
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file.getInputStream())) {
            grabber.start();
            result.put("durationSeconds", (double) grabber.getLengthInTime() / 1_000_000.0);
            result.put("frameRate", grabber.getFrameRate());
            result.put("width", grabber.getImageWidth());
            result.put("height", grabber.getImageHeight());
            result.put("format", grabber.getFormat());
            grabber.stop();
        } catch (Exception e) {
            logger.error("视频文件解析异常:{}", e.getMessage());
            result.put("success", false);
            result.put("error", e.getClass().getSimpleName());
            result.put("message", e.getMessage());
        } finally {
            try {
                file.getInputStream().close();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        return result.toJSONString();
    }

    /**
     * 判断文件是否为视频类型文件
     *
     * @param file
     * @return
     */
    public static boolean isVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;

        // 1. MIME判断
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("video/")) return true;

        // 2. 扩展名判断
        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            String lower = fileName.toLowerCase();
            if (lower.matches(".*\\.(mp4|avi|mov|wmv|flv|mkv|3gp|webm)$")) return true;
        }

        // 3. Magic Number 检测
        return isVideoFileByMagic(file);
    }

    private static boolean isVideoFileByMagic(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header, 0, 12);
            if (bytesRead < 12) return false;

            // 常见视频文件头匹配
            String hex = bytesToHex(header).toUpperCase();

            // MP4 文件头（前4字节一般是ftyp）
            if (hex.contains("66747970")) return true; // "ftyp"
            // AVI
            if (hex.startsWith("52494646") && hex.endsWith("41564920")) return true; // RIFF....AVI
            // MKV / WebM
            if (hex.startsWith("1A45DFA3")) return true;
            // FLV
            if (hex.startsWith("464C56")) return true;
            // QuickTime MOV
            if (hex.contains("66747970")) return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


}
