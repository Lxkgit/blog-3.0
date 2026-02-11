package com.blog.file.controller;

import com.alibaba.fastjson2.JSON;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.netty.domain.dto.NettyPacket;
import com.blog.file.netty.domain.dto.file.NettySyncFileDto;
import com.blog.file.netty.domain.enums.NettyTopic;
import com.blog.file.netty.service.NettyServer;
import com.blog.file.netty.service.NettySyncFileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 测试接口
 * @Author lxk
 * @CreateTime 2025-06-10
 */

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private NettySyncFileService nettyFileSyncService;

    @Resource
    private NettyServer nettyServer;

    @GetMapping("/get")
    public Result getTest() {
        System.out.println("测试方法调用");

        NettyPacket<String> nettyPacket = NettyPacket.buildRequest(NettyTopic.BLOG_FILE_SYNC, "test");

        return ResultFactory.buildSuccessResult(nettyServer.sendByRegisterIdLimitTime("1:2ecfb95116de4967afe7710e11ac00b4", null,
                JSON.toJSONString(nettyPacket), 2 * 60));
    }
}
