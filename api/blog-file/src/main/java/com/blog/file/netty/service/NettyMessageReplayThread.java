package com.blog.file.netty.service;

import com.alibaba.fastjson2.JSONObject;
import com.blog.file.netty.domain.dto.NettyReplayMessage;
import com.blog.redis.constant.NettyRedisConstant;
import com.blog.redis.service.RedisService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Component
public class NettyMessageReplayThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NettyMessageReplayThread.class);

    @Resource
    private RedisService redisService;

    @Resource
    private NettyServer nettyServer;

    @Override
    // 忽略无限循环与忙等待报错
    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    public void run() {
        logger.info("===== netty 消息重发线程启动 =====");
        while (true) {
            try {
                // 消息重发队列中去掉netty收到响应的消息
                Set<Object> allValues = redisService.getSetByKey(NettyRedisConstant.NETTY_RECEIVE_QUEUE);
                redisService.delKey(NettyRedisConstant.NETTY_RECEIVE_QUEUE);
                if (CollectionUtils.isNotEmpty(allValues)) {
                    for (Object value : allValues) {
                        logger.info("收到消息响应: {}", value.toString());
                        if (redisService.hasHashKey(NettyRedisConstant.NETTY_SEND_QUEUE, value.toString())) {
                            redisService.deleteHashByKey(NettyRedisConstant.NETTY_SEND_QUEUE, value.toString());
                        }
                    }
                }

                Map<Object, Object> map = redisService.getAllHash(NettyRedisConstant.NETTY_SEND_QUEUE);
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    String key = entry.getKey().toString();

                    NettyReplayMessage replayMessage = JSONObject.parseObject((String) entry.getValue(), NettyReplayMessage.class);
                    // 消息最近发送时间
                    LocalDateTime lastSendTime = replayMessage.getLastSendTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    // 当前时间
                    LocalDateTime nowDate = LocalDateTime.now();

                    if (replayMessage.getRetryType() == 1) {
                        // 计算两个时间点之间的时间差
                        Duration duration = Duration.between(lastSendTime, nowDate);
                        if (replayMessage.getLimitCount() != 0) {
                            if (replayMessage.getTryCount() <= replayMessage.getLimitCount()) {
                                resendMessage(duration, key, replayMessage);
                            } else {
                                logger.info("netty 消息超过发送次数，丢弃此消息 msg:{}", replayMessage.getMessage());
                                redisService.deleteHashByKey(NettyRedisConstant.NETTY_SEND_QUEUE, entry.getKey());
                            }
                        } else {
                            resendMessage(duration, key, replayMessage);
                        }
                    } else if (replayMessage.getRetryType() == 2) {
                        // 消息首次发送时间
                        LocalDateTime firstSendTime = replayMessage.getFirstSendTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        Duration effectiveDuration = Duration.between(firstSendTime, nowDate);
                        // 首次发送消息与当前时间大于指定消息有效时间 丢弃消息
                        if (effectiveDuration.toMinutes() > replayMessage.getEffectiveTime()) {
                            logger.info("netty 消息超过有效时间，丢弃此消息 msg:{}", replayMessage.getMessage());
                            redisService.deleteHashByKey(NettyRedisConstant.NETTY_SEND_QUEUE, entry.getKey());
                        } else {
                            Duration lastDuration = Duration.between(lastSendTime, nowDate);
                            replayMessage.setLastSendTime(new Date());
                            resendMessage(lastDuration, key, replayMessage);
                        }
                    }
                }

                Thread.sleep(30 * 1000);
            } catch (InterruptedException e) {
                logger.error("netty 消息重发线程异常:{}", e.getMessage(), e);
            }
        }
    }

    /**
     * 发送netty消息
     *
     * @param duration      时间差
     * @param key           消息key
     * @param replayMessage 重发消息内容
     */
    private void resendMessage(Duration duration, String key, NettyReplayMessage replayMessage) {
        // 上次发送消息与当前时间大于消息发送间隔
        if (duration.toMinutes() > 5) {
            logger.info("netty 重发消息: {}", replayMessage.toString());
            nettyServer.sendByRegisterIdNotRetry(key, replayMessage.getMessage());
            replayMessage.setTryCount(replayMessage.getTryCount() + 1);
            redisService.setHash(NettyRedisConstant.NETTY_SEND_QUEUE, key, JSONObject.toJSONString(replayMessage), 24*60*60);
        }
    }
}
