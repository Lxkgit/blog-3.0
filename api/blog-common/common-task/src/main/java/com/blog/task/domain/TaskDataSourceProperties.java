package com.blog.task.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-02-02
 */

@Data
@ConfigurationProperties(prefix = "task.datasource")
public class TaskDataSourceProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
}

