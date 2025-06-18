package com.blog.file.netty.service;

import com.alibaba.fastjson2.JSONObject;
import com.blog.file.netty.domain.dto.NettyReplayMessage;
import com.blog.redis.constant.NettyRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NettyMessageReplayThread implements Runnable {

    @Resource
    private RedisService redisService;

    @Resource
    private NettyServer nettyServer;

    @Override
    public void run() {
        Set<Object> allValues = redisService.getSetByKey(NettyRedisConstant.NETTY_RECEIVE_QUEUE);
        redisService.delKey(NettyRedisConstant.NETTY_RECEIVE_QUEUE);
        if (CollectionUtils.isNotEmpty(allValues)) {
            for (Object value : allValues) {
                if (redisService.hasHashKey(NettyRedisConstant.NETTY_SEND_QUEUE, value.toString())) {
                    redisService.deleteAllHash(NettyRedisConstant.NETTY_SEND_QUEUE, value.toString());
                }
            }
        }

        Map<Object, Object> map = redisService.getAllHash(NettyRedisConstant.NETTY_SEND_QUEUE);
        map.forEach((k, v) -> {
            String key = k.toString();
            NettyReplayMessage replayMessage = JSONObject.parseObject((String) v, NettyReplayMessage.class);
            // 消息发送时间
            LocalDateTime startDate = replayMessage.getSendTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime  nowDate = LocalDateTime.now();
            // 计算两个时间点之间的时间差
            Duration duration = Duration.between(startDate, nowDate);
            // 检查时间差是否超过五分钟
            if (duration.toMinutes() > 5) {
                if (replayMessage.getTryTime() < 5) {
                    nettyServer.channelWriteByRegisterId(key, JSONObject.toJSONString(replayMessage), true);
                    replayMessage.setTryTime(replayMessage.getTryTime() + 1);
                    redisService.setHash(NettyRedisConstant.NETTY_SEND_QUEUE, key, JSONObject.toJSONString(replayMessage));
                }
            }
        });


        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
