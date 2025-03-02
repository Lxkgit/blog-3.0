package com.blog.content;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.dromara.easyes.starter.register.EsMapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EsMapperScan("com.blog.content.mapper.es")
@Import(RocketMQAutoConfiguration.class)
public class ContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }


}
