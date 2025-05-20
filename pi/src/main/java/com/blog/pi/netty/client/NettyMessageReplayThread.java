package com.blog.pi.netty.client;

import com.blog.pi.netty.dto.NettyReplayMessage;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NettyMessageReplayThread implements Runnable {

    @Resource
    private RedisService redisService;

    private ConcurrentHashMap<String, NettyReplayMessage> replayMap = new ConcurrentHashMap<>();

    @Resource
    private NettyClient nettyClient;

    @Override
    public void run() {
        if (!CollectionUtils.isEmpty(replayMap) && nettyClient.getChannelActive()) {
            replayMap.forEach((k, v) -> {

            });
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
