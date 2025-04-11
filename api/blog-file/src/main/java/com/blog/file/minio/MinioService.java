package com.blog.file.minio;

import cn.hutool.extra.spring.SpringUtil;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.Resource;
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

@Service
public class MinioService {

    @Resource
    private MinioClient minioClient;

    /**
     * 文件上传
     *
     * @param bucketName    桶
     * @param fileName      文件名
     * @param multipartFile 文件
     * @return
     * @throws IOException
     */
    public String uploadFile(String bucketName, String fileName, MultipartFile multipartFile) throws IOException {
        String url;
        MinioClient minioClient = SpringUtil.getBean(MinioClient.class);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                /**
                 * bucket权限-读写
                 */
                String READ_WRITE = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\",\"s3:PutObject\",\"s3:AbortMultipartUpload\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(READ_WRITE).build());
            }
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, multipartFile.getSize(), -1)
                    .contentType(multipartFile.getContentType()).build()
            );
            //路径获取
            url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucketName).object(fileName).
                    method(Method.GET).build());
            url = url.substring(0, url.indexOf('?'));
            //常规访问路径获取
            return url;
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }




}
