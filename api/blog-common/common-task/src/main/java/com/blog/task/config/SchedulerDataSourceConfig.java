package com.blog.task.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
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

@Data
@Configuration
@EnableScheduling
@ConfigurationProperties(prefix = "task.datasource")
@MapperScan(basePackages = "com.blog.task.mapper", sqlSessionTemplateRef = "schedulerSqlSessionTemplate")
public class SchedulerDataSourceConfig {

    private String url;
    private String username;
    private String password;
    private String driverClassName;

    @Bean(name = "schedulerDataSource")
    public DataSource schedulerDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(this.url);
        ds.setUsername(this.username);
        ds.setPassword(this.password);
        ds.setDriverClassName(this.driverClassName);
        return ds;
    }

    // MyBatis 配置
    @Bean(name = "schedulerSqlSessionFactory")
    public SqlSessionFactory schedulerSqlSessionFactory(@Qualifier("schedulerDataSource") DataSource ds) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(ds);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/**/*.xml"));
        return factoryBean.getObject();
    }

    @Bean(name = "schedulerSqlSessionTemplate")
    public SqlSessionTemplate schedulerSqlSessionTemplate(@Qualifier("schedulerSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}

