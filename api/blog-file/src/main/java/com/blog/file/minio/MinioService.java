package com.blog.file.minio;

import com.blog.core.domain.file.files.entity.FileUploadLog;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.FileUploadLogMapper;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Description Minio服务
 * @Author lxk
 * @CreateTime 2025-04-12
 */

@Service
public class MinioService {

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    @Value("${minio.ip}")
    private String ip;

    @Value("${minio.port}")
    private Integer port;

    @Value("${minio.bucket}")
    private String bucket;

    @Resource
    private MinioClient minioClient;

    @Resource
    private FileUploadLogMapper fileUploadLogMapper;

    public String uploadFile(MultipartFile file, String path) throws ServiceException {
        Integer userId = SecurityUtil.getLoginUser().getId();
        String userName = SecurityUtil.getLoginUser().getUsername();
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);

        // 记录文件上传信息
        FileUploadLog fileUploadLog = new FileUploadLog(userId, file.getOriginalFilename(), fileType, 0, userName, new Date());
        fileUploadLogMapper.insert(fileUploadLog);
        try {
            InputStream inputStream = file.getInputStream();
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            // /files 为nginx代理路径
//            String fileUrl = ip + "/files/" + bucket + path;
            String fileUrl = ip + ":9000/" + bucket + path;
            // 文件上传成功
            FileUploadLog fileLog = new FileUploadLog();
            fileLog.setId(fileUploadLog.getId());
            fileLog.setFileUrl(fileUrl);
            fileLog.setUploadState(1);
            fileUploadLogMapper.updateById(fileLog);

            return fileUrl;
        } catch (Exception e) {
            // 文件上传失败
            FileUploadLog fileLog = new FileUploadLog();
            fileLog.setId(fileUploadLog.getId());
            fileLog.setErrorMsg(e.getMessage());
            fileLog.setUploadState(2);
            fileUploadLogMapper.updateById(fileLog);
            logger.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    public String getFileUrl(String minioPath, String fileName) {
        return ip + ":9000/" + bucket + minioPath + "/" + fileName;
    }

    /**
     * 删除指定文件
     *
     * @param path     文件路径
     * @param fileName 文件名称
     */
    public void deleteFile(String path, String fileName) throws ServiceException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(path + "/" + fileName)
                            .build());
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 文件授权
     *
     * @param path
     * @param time
     * @return
     */
    public String authFile(String path, Integer time) throws ServiceException {
        try {
            // 生成标准预签名URL（路径不含/minio）

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(path) // 如 "user1/article/doc.pdf"
                            .expiry(time, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 导出minio文件到服务器指定位置
     *
     * @param minioFileName MinIO中的文件名（带路径）
     * @param path          本地保存的文件名
     * @return
     * @throws ServiceException
     */
    public String exportFile(String minioFileName, String path) throws ServiceException {
        // 1. 从MinIO下载文件流
        try (InputStream fileStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(minioFileName)
                .build())) {

            // 2. 确保目标目录存在
            Path exportPath = Paths.get(path);
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }
            String fileName = extractFileName(minioFileName);

            // 3. 保存到本地文件
            Path targetPath = exportPath.resolve(fileName);
            Files.copy(fileStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 将服务器本地文件写入minio
     *
     * @param localFilePath 服务器中文件位置
     * @param minioPath     minio中文件位置
     */
    public void importFile(String localFilePath, String minioPath) {
        logger.info("minio 导入文件: localFilePath:{} minioPath:{}", localFilePath, minioPath);
        File file = new File(localFilePath);
        if (!file.exists() || !file.isFile()) {
            logger.error("minio 文件导入异常: 文件{}不存在", localFilePath);
        }
        try {

            InputStream inputStream = Files.newInputStream(file.toPath());
            String contentType = Files.probeContentType(Paths.get(localFilePath));
            ObjectWriteResponse response = minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(minioPath + "/" + getFileName(localFilePath))
                    .stream(inputStream, file.length(), -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            logger.error("minio 文件导入异常: {}", e.getMessage(), e);
        }
    }

    public String getFileName(String filePath) {
        // 处理空路径或非法输入
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        // 找到最后一个分隔符位置（兼容 Windows/Linux）
        int lastSeparator = Math.max(
                filePath.lastIndexOf('/'),
                filePath.lastIndexOf('\\')
        );
        // 截取文件名部分
        return (lastSeparator >= 0)
                ? filePath.substring(lastSeparator + 1)
                : filePath;
    }

    /**
     * 解析minio文件名称
     *
     * @param minioObjectName MinIO中的文件名（带路径）
     * @return 文件名称
     */
    private static String extractFileName(String minioObjectName) {
        if (StringUtils.isBlank(minioObjectName)) {
            return "";
        }

        // 处理可能包含多个分隔符的情况
        return StringUtils.substringAfterLast(minioObjectName, "/");
    }


}
