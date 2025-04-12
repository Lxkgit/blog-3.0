package com.blog.file.minio;

import cn.hutool.extra.spring.SpringUtil;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Description Minio服务
 * @Author lxk
 * @CreateTime 2025-04-12
 */

@Slf4j
@Service
public class MinioService {

    @Resource
    private MinioClient minioClient;

    public boolean uploadFile(MultipartFile file, String path) {

        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket("blog")
                    .object(path)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            //常规访问路径获取
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }
}
