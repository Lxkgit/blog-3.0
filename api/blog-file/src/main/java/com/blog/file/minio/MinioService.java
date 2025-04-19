package com.blog.file.minio;

import com.blog.core.domain.file.files.entity.FileUploadLog;
import com.blog.core.exception.ServiceException;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.mapper.FileUploadLogMapper;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Description Minio服务
 * @Author lxk
 * @CreateTime 2025-04-12
 */

@Slf4j
@Service
public class MinioService {

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

            log.info("img_url: {}", response.toString());

            // /files 为nginx代理路径
            String fileUrl = ip + "/files/" + bucket + path;
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
            log.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 删除指定文件
     *
     * @param path 文件路径
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
            log.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     *
     * @param path
     * @param time
     * @return
     */
    public String authFile(String path, Integer time) throws ServiceException {
        try  {
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
            log.error(e.getMessage());
            throw new ServiceException(e.getMessage());
        }

    }

}
