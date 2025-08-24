package com.blog.file.controller;

import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.netty.service.NettyFileSyncService;
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
    private NettyFileSyncService nettyFileSyncService;

    @GetMapping("/get")
    public Result getTest() {
        System.out.println("测试方法调用");
        nettyFileSyncService.deleteTempFile("test-11", "3h");
        return ResultFactory.buildSuccessResult();
    }
}
