package com.blog.file.netty.service;

import io.netty.channel.ChannelId;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description Netty 用户消息处理
 * @Author lxk
 * @CreateTime 2024-09-05
 */

@Slf4j
@Service
public class NettyUserService {

//    @Resource
//    private RedisUtil redisUtil;

//    @Resource
//    private NettyServer nettyServer;
//
//    @Resource
//    private UserService userService;
//
//    public BlogUser UserRegister(String registerId, ChannelId channelId, String username) {
//
//        BlogUser blogUser;
//        try {
//            String value = (String) redisUtil.getString("username:" + username);
//
//            if (value == null || value.equals("")) {
//                blogUser = userService.getBlogUserByUsername(username);
//                redisUtil.setString("username:" + username, JSONObject.toJSONString(blogUser));
//            } else {
//                blogUser = JSONObject.parseObject(value, BlogUser.class);
//            }
//            return blogUser;
//        } catch (Exception e) {
//            // 消息响应
//            NettyPacket<String> nettyResponse = NettyPacket.buildResponse(registerId, "用户不存在");
//            nettyResponse.setTopic("topic");
//            nettyServer.channelWriteByChannelId(channelId, JSONObject.toJSONString(nettyResponse));
//            nettyServer.close(channelId);
//            e.printStackTrace();
//        }
//        return null;
//
//    }
}
