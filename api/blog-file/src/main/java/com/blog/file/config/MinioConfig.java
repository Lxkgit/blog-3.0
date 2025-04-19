package com.blog.file.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description minio配置
 * @Author lxk
 * @CreateTime 2025-04-12
 */

@Configuration
public class MinioConfig {

    @Value("${minio.ip}")
    private String ip;

    @Value("${minio.port}")
    private Integer port;

    @Value("${minio.username}")
    private String username;

    @Value("${minio.password}")
    private String password;

    @Value("${minio.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        // curl -I http://123.207.202.131:9000/minio/health/live
        // curl -I http://123.207.202.131/files/minio/health/live
        return MinioClient.builder().endpoint(ip + ":" + port).credentials(username, password).build();
    }
}
