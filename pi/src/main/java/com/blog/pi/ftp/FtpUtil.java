package com.blog.pi.ftp;

import com.blog.pi.config.PiSystemConfig;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @description:
 * @Author: lxk
 * @date 2024/1/2 16:40
 */

/**
 * 多线程安全 FTP 工具类
 * 兼容原有方法签名：uploadFtpFile / downloadFtpFile
 */
@Service
public class FtpUtil {

    private static final Logger logger = LoggerFactory.getLogger(FtpUtil.class);

    @Value("${ftp.ip}")
    private String ip;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;

    /**
     * 多线程线程池
     */
    private final ExecutorService executorService =
            Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY = 3;

    /**
     * 每次重试等待时间（毫秒）
     */
    private static final long RETRY_INTERVAL = 2000;

    // ==================== FTP 基础连接管理 ====================

    private FTPClient createFtpClient() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(10000);
        ftpClient.connect(ip, port);

        if (!ftpClient.login(username, password)) {
            ftpClient.disconnect();
            throw new IOException("FTP 登录失败");
        }

        ftpClient.setControlEncoding("UTF-8");
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
        ftpClient.setSoTimeout(3 * 60 * 1000);
        ftpClient.setDataTimeout(Duration.ofMinutes(5));
        ftpClient.setRemoteVerificationEnabled(false);
        ftpClient.sendNoOp(); // 验证连接
        return ftpClient;
    }

    private void disconnectFtp(FTPClient ftpClient) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                logger.warn("关闭 ftp 连接异常: {}", e.getMessage());
            }
        }
    }

    private void createDirectoryByPathName(FTPClient ftpClient, String pathName) throws IOException {
        String[] dirList = pathName.split("/");
        for (String dir : dirList) {
            if (dir == null || dir.isEmpty()) continue;
            if (!ftpClient.changeWorkingDirectory(dir)) {
                ftpClient.makeDirectory(dir);
                ftpClient.changeWorkingDirectory(dir);
            }
        }
    }

    // ==================== 上传功能（带重试） ====================

    /**
     * 上传文件（线程安全 + 自动重试）
     */
    public boolean uploadFtpFile(String sourceFilePath, String sourceFileName,
                                 String targetFilePath, String targetFileName) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            if (uploadOnce(sourceFilePath, sourceFileName, targetFilePath, targetFileName)) {
                logger.info("文件上传成功: {}/{} -> {}/{}", sourceFilePath, sourceFileName, targetFilePath, targetFileName);
                return true;
            } else {
                logger.warn("第 {} 次上传失败，准备重试...", attempt);
                sleepBeforeRetry(attempt);
            }
        }
        logger.error("文件上传失败，重试 {} 次后仍未成功: {}/{}", MAX_RETRY, sourceFilePath, sourceFileName);
        return false;
    }

    private boolean uploadOnce(String sourceFilePath, String sourceFileName,
                               String targetFilePath, String targetFileName) {
        FTPClient ftpClient = null;
        Path filePath = Paths.get(sourceFilePath, sourceFileName);
        File file = filePath.toFile();

        try {
            ftpClient = createFtpClient();
            createDirectoryByPathName(ftpClient, targetFilePath);
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                String fn = new String(targetFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
                logger.info("开始上传文件: {}", filePath.getFileName());
                boolean success = ftpClient.storeFile(fn, inputStream);
                try {
                    ftpClient.completePendingCommand();
                } catch (SocketTimeoutException e) {
                    logger.warn("completePendingCommand 超时，但文件可能已上传完成: {}", e.getMessage());
                }
                logger.info("ftp 文件上传结果: {}", success);
                return success;
            }
        } catch (Exception e) {
            logger.error("ftp 上传失败: {}", e.getMessage(), e);
            return false;
        } finally {
            disconnectFtp(ftpClient);
        }
    }

    // ==================== 下载功能（带重试） ====================

    /**
     * 下载文件（线程安全 + 自动重试）
     */
    public boolean downloadFtpFile(String serviceFilePath, String serviceFileName,
                                   String localFilePath, String localFileName) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            if (downloadOnce(serviceFilePath, serviceFileName, localFilePath, localFileName)) {
                logger.info("文件下载成功: {}/{} -> {}/{}", serviceFilePath, serviceFileName, localFilePath, localFileName);
                return true;
            } else {
                logger.warn("第 {} 次下载失败，准备重试...", attempt);
                sleepBeforeRetry(attempt);
            }
        }
        logger.error("文件下载失败，重试 {} 次后仍未成功: {}/{}", MAX_RETRY, serviceFilePath, serviceFileName);
        return false;
    }

    private boolean downloadOnce(String serviceFilePath, String serviceFileName,
                                 String localFilePath, String localFileName) {
        FTPClient ftpClient = null;
        try {
            ftpClient = createFtpClient();
            createDir(localFilePath);
            ftpClient.changeWorkingDirectory(
                    new String(serviceFilePath.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1)
            );

            File localFile = new File(localFilePath, localFileName);
            try (OutputStream os = new FileOutputStream(localFile)) {
                boolean success = ftpClient.retrieveFile(
                        new String(serviceFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1),
                        os
                );
                ftpClient.completePendingCommand();
                logger.info("ftp 文件下载结果: {}", success);
                return success;
            }
        } catch (IOException e) {
            logger.error("ftp 文件下载失败: {}", e.getMessage(), e);
            return false;
        } finally {
            disconnectFtp(ftpClient);
        }
    }

    // ==================== 目录与工具 ====================

    public boolean createDir(String dirPath) {
        try {
            Files.createDirectories(Paths.get(dirPath));
            logger.info("目录创建成功或已存在: {}", dirPath);
            return true;
        } catch (IOException e) {
            logger.error("目录创建失败: {} -> {}", dirPath, e.getMessage());
            return false;
        }
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(RETRY_INTERVAL * attempt);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * 应用关闭时关闭线程池
     */
    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}