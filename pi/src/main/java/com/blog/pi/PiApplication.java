package com.blog.pi;

import com.blog.pi.netty.client.NettyMessageReplayThread;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class PiApplication {



    public static void main(String[] args) {
        SpringApplication.run(PiApplication.class, args);
        ExecutorService fixedPool = Executors.newFixedThreadPool(1);
        fixedPool.execute(new NettyMessageReplayThread());
    }

}
