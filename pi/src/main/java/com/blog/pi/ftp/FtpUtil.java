package com.blog.pi.ftp;

import com.blog.pi.config.PiSystemConfig;
import jakarta.annotation.Resource;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @description:
 * @Author: lxk
 * @date 2024/1/2 16:40
 */

@Service
public class FtpUtil {

    private static final Logger logger = LoggerFactory.getLogger(FtpUtil.class);

    @Resource
    private PiSystemConfig piSystemConfig;

    private FTPClient ftpClient;

    @Value("${ftp.ip}")
    private String ip;

    @Value("${ftp.port}")
    private int port;

    @Value("${ftp.username}")
    private String username;

    @Value("${ftp.password}")
    private String password;

    private boolean init() {
        ftpClient = new FTPClient();
        if (ftpClient.isConnected()) {
            return true;
        }
        logger.info("开始连接ftp");
        int reply;
        try {
            ftpClient.setConnectTimeout(10000);
            ftpClient.connect(ip, port);
            ftpClient.login(username, password);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
            } else {
                logger.info("ftp 连接成功");
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            }
        } catch (Exception e) {
            logger.error("ftp 连接异常: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 创建ftp目录并切换ftp工作目录
     *
     * @param pathName 文件目录
     * @return void
     * @throws IOException
     * @auther 27919
     * @date 2020年5月16日
     */
    private void createDirectoryByPathName(String pathName) throws IOException {
        String[] dirList = pathName.split("/");
        for (String dir : dirList) {
            logger.info("ftp 当前目录 : {}", dir);
            if (!ftpClient.changeWorkingDirectory(dir)) {
                ftpClient.makeDirectory(dir);
                ftpClient.changeWorkingDirectory(dir);
            }
        }
    }

    /**
     * 上传文件到ftp服务器
     *
     * @param sourceFilePath 源文件位置
     * @param sourceFileName 源文件名称
     * @param targetFilePath 服务器文件路径
     * @param targetFileName 服务器文件名称
     * @return 文件上传是否成功
     */
    public boolean uploadFtpFile(String sourceFilePath, String sourceFileName, String targetFilePath, String targetFileName) {
        logger.info("ftp 上传文件  sourcePath: {}, sourceFileName: {}, targetName: {}, targetFileName: {}", sourceFilePath, sourceFileName, targetFilePath, targetFileName);
        boolean initSuccess = this.init();
        if (!initSuccess) {
            return false;
        }
        InputStream inputStream = null;
        try {
            File file = new File(sourceFilePath + "/" + sourceFileName);
            byte[] bytes = Files.readAllBytes(file.toPath());
            inputStream = new ByteArrayInputStream(bytes);
            createDirectoryByPathName(new String(targetFilePath.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            String fn = new String(targetFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            boolean success = ftpClient.storeFile(fn, inputStream);
            logger.info("ftp 文件上传结果:{}", success);
            return success;
        } catch (Exception e) {
            logger.error("ftp 文件上传失败", e);
        } finally {
            if (!ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error("ftp 文件上传失败:{}", e.getMessage(), e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("ftp 文件上传失败:{}", e.getMessage(), e);
                }
            }
        }
        return false;
    }


    /**
     * 下载服务器文件
     *
     * @param serviceFilePath 服务器文件目录
     * @param serviceFileName 服务器文件名称
     * @param localFilePath   本地存放文件相对目录
     * @param localFileName   本地存放文件名称
     * @return 下载结果
     */
    public boolean downloadFtpFile(String serviceFilePath, String serviceFileName, String localFilePath, String localFileName) {
        logger.info("ftp 下载文件 pathName:{} fileName:{}", serviceFilePath, serviceFileName);
        init();
        try {
            createDir(localFilePath);
            ftpClient.changeWorkingDirectory(new String(serviceFilePath.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            File localFile = new File(localFilePath + File.separatorChar + localFileName);
            OutputStream os = new FileOutputStream(localFile);
            boolean success = ftpClient.retrieveFile(new String(serviceFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), os);
            os.close();
            logger.info("ftp 文件下载结果:{}", success);
            return success;
        } catch (IOException e) {
            logger.error("ftp 文件下载失败:{}", e.getMessage(), e);
        } finally {
            if (!ftpClient.isConnected()) {
                try {
                    ftpClient.completePendingCommand();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error("ftp 文件下载失败:{}", e.getMessage(), e);
                }
            }
        }
        return false;
    }

    /**
     * 创建本地目录（自动创建父目录）
     * @param dirPath 目录路径
     * @return 是否创建成功（已存在视为成功）
     */
    public boolean createDir(String dirPath) {
        try {
            Files.createDirectories(Paths.get(dirPath));
            logger.info("目录创建成功或已存在: {}", dirPath);
            return true;
        } catch (IOException e) {
            logger.error("目录创建失败: {} -> {}", dirPath, e.getMessage(), e);
            return false;
        }
    }


    /**
     * 删除ftp服务器文件
     *
     * @param pathName 文件目录
     * @param fileName 文件名称
     * @return 删除结果
     */
    public boolean removeFile(String pathName, String fileName) {
        logger.info("ftp 删除文件  pathName: {}, fileName: {}", pathName, fileName);
        init();
        try {
            ftpClient.changeWorkingDirectory(new String(pathName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            boolean success = ftpClient.deleteFile(new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
            logger.info("ftp 文件删除结果 " + success);
            return success;
        } catch (IOException e) {
            logger.error("ftp 文件删除失败", e);
        } finally {
            if (!ftpClient.isConnected()) {
                try {
                    ftpClient.completePendingCommand();
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error("ftp 文件删除失败", e);
                }
            }
        }
        return false;
    }


}
