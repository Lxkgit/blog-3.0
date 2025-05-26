package com.blog.pi.netty.client;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.blog.pi.netty.dto.NettyReplayMessage;
import com.blog.redis.constant.NettyRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

@Component
public class NettyMessageReplayThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NettyMessageReplayThread.class);

    private static RedisService redisService;

    private static NettyClient nettyClient;

    @Resource
    private void setRedisService(RedisService redisService) {
        NettyMessageReplayThread.redisService = redisService;
    }

    @Resource
    private void setNettyClient(NettyClient nettyClient) {
        NettyMessageReplayThread.nettyClient = nettyClient;
    }

    @Override
    public void run() {
        logger.info("netty 消息重发线程启动");
        while (true) {
            Set<Object> allValues = redisService.getSetByKey(NettyRedisConstant.NETTY_RECEIVE_QUEUE);
            if (CollectionUtils.isNotEmpty(allValues)) {
                logger.info("netty 收到消息：{}", allValues);
                redisService.delKey(NettyRedisConstant.NETTY_RECEIVE_QUEUE);
                if (CollectionUtils.isNotEmpty(allValues)) {
                    for (Object value : allValues) {
                        if (redisService.hasHashKey(NettyRedisConstant.NETTY_SEND_QUEUE, value.toString())) {
                            redisService.deleteAllHash(NettyRedisConstant.NETTY_SEND_QUEUE, value.toString());
                        }
                    }
                }
            }

            Map<Object, Object> map = redisService.getAllHash(NettyRedisConstant.NETTY_SEND_QUEUE);
            if (CollectionUtils.isNotEmpty(map) || nettyClient.getChannelActive()) {
                map.forEach((k, v) -> {
                    String key = k.toString();
                    NettyReplayMessage replayMessage = (NettyReplayMessage) v;
                    // 消息发送时间
                    LocalDateTime startDate = replayMessage.getSendTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    LocalDateTime  nowDate = LocalDateTime.now();
                    // 计算两个时间点之间的时间差
                    Duration duration = Duration.between(startDate, nowDate);
                    // 检查时间差是否超过五分钟
                    if (duration.toMinutes() > 5) {
                        if (replayMessage.getTryTime() < 5) {
                            logger.info("netty 消息重发 requestId: {} msg:{}", k, v);
                            nettyClient.sendMsg(key, replayMessage.getMessage(), false);
                            replayMessage.setTryTime(replayMessage.getTryTime() + 1);
                            redisService.setHash(NettyRedisConstant.NETTY_SEND_QUEUE, key, replayMessage);
                        } else {
                            // 重发次数超过五次的数据直接丢弃
                            redisService.deleteAllHash(NettyRedisConstant.NETTY_SEND_QUEUE, key);
                        }
                    }
                });
            }

            try {
                Thread.sleep(20*1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
