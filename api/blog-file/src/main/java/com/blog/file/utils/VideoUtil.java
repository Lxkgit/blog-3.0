package com.blog.file.utils;

import com.alibaba.fastjson2.JSONObject;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-10-13
 */

public class VideoUtil {

    private static final Logger logger = LoggerFactory.getLogger(VideoUtil.class);

    /**
     * 解析 MultipartFile 类型的视频文件
     */
    public static String resolveVideo(MultipartFile file) {
        if (!isVideoFile(file)) {
            return buildErrorResult("MultipartFile", "上传文件不是视频文件");
        }

        File tempFile = null;
        try {
            // MultipartFile 写入临时文件
            tempFile = File.createTempFile("video-", ".tmp");
            try (InputStream in = file.getInputStream();
                 OutputStream out = new FileOutputStream(tempFile)) {
                in.transferTo(out);
            }

            return parseVideoInfo(tempFile);

        } catch (Exception e) {
            return buildErrorResult(e, null);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (!tempFile.delete()) {
                    logger.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 解析 File 类型的视频文件
     */
    public static String resolveVideo(File file) {
        if (!isVideoFile(file)) {
            return buildErrorResult("File", "导入文件不是视频文件");
        }
        return parseVideoInfo(file);
    }

    /**
     * 使用 FFmpegFrameGrabber 解析视频信息
     */
    private static String parseVideoInfo(File file) {
        JSONObject result = new JSONObject();
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file)) {
            grabber.start();

            result.put("durationSeconds", grabber.getLengthInTime() / 1_000_000.0);
            result.put("frameRate", grabber.getFrameRate());
            result.put("width", grabber.getImageWidth());
            result.put("height", grabber.getImageHeight());
            result.put("format", grabber.getFormat());
            result.put("success", true);

            grabber.stop();
        } catch (Exception e) {
            return buildErrorResult(e, file);
        }
        return result.toJSONString();
    }

    /**
     * 判断 MultipartFile 是否为视频文件
     */
    public static boolean isVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return false;

        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("video/")) return true;

        String fileName = file.getOriginalFilename();
        return isVideoFile(fileName);
    }

    /**
     * 判断 File 是否为视频文件
     */
    public static boolean isVideoFile(File file) {
        if (file == null || !file.exists() || file.isDirectory()) return false;
        return isVideoFile(file.getName());
    }

    /**
     * 判断文件名是否为视频文件
     */
    private static boolean isVideoFile(String fileName) {
        if (fileName == null) return false;
        String lower = fileName.toLowerCase();
        return lower.matches(".*\\.(mp4|avi|mov|wmv|flv|mkv|3gp|webm|m4v)$");
    }

    /**
     * 统一异常结果封装
     */
    private static String buildErrorResult(Exception e, File file) {
        JSONObject result = new JSONObject();
        String filePath = (file != null) ? file.getAbsolutePath() : "N/A";
        logger.error("视频文件解析异常: {} - 文件: {}", e.getMessage(), filePath, e);
        result.put("success", false);
        result.put("error", e.getClass().getSimpleName());
        result.put("message", e.getMessage());
        result.put("file", filePath);
        return result.toJSONString();
    }

    /**
     * 非异常类错误（例如文件类型不支持）
     */
    private static String buildErrorResult(String error, String message) {
        JSONObject result = new JSONObject();
        result.put("success", false);
        result.put("error", error);
        result.put("message", message);
        return result.toJSONString();
    }
}
