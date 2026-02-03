package com.blog.task.config;

import com.blog.task.domain.TaskDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-01-30
 */

@Configuration
@EnableScheduling
@EnableConfigurationProperties(TaskDataSourceProperties.class)
@MapperScan(basePackages = "com.blog.task.mapper", sqlSessionTemplateRef = "schedulerSqlSessionTemplate")
public class SchedulerDataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerDataSourceConfig.class);

    private final TaskDataSourceProperties properties;

    public SchedulerDataSourceConfig(TaskDataSourceProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "schedulerDataSource")
    public DataSource schedulerDataSource() {
        logger.info("任务服务配置 url: {} username: {} password: {} driverClassName: {}",
                properties.getUrl(),
                properties.getUsername(),
                properties.getPassword(),
                properties.getDriverClassName());

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(properties.getUrl());
        ds.setUsername(properties.getUsername());
        ds.setPassword(properties.getPassword());
        ds.setDriverClassName(properties.getDriverClassName());
        return ds;
    }

    @Bean(name = "schedulerSqlSessionFactory")
    public SqlSessionFactory schedulerSqlSessionFactory(
            @Qualifier("schedulerDataSource") DataSource ds) throws Exception {

        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(ds);

        // 只加载 scheduler 模块自己的 mapper
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath*:mapper/*.xml")
        );

        // 可选：指定实体别名包（强烈建议）
        factoryBean.setTypeAliasesPackage("com.blog.task.entity");

        // 可选：独立 MyBatis 配置（避免和主工程冲突）
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(false);

        factoryBean.setConfiguration(configuration);

        return factoryBean.getObject();
    }


    @Bean(name = "schedulerSqlSessionTemplate")
    public SqlSessionTemplate schedulerSqlSessionTemplate(
            @Qualifier("schedulerSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {

        return new SqlSessionTemplate(sqlSessionFactory);
    }


}

