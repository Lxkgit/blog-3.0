package com.dmg.kucun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class KucunApplication {

    public static void main(String[] args) {
        SpringApplication.run(KucunApplication.class, args);
    }

    /**
     *
     * 用于远程通信 远程接口调用
     * @param
     * @return
     * @throws Exception
     */
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
