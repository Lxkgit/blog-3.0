package com.blog.file.controller;

import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.netty.service.NettyFileSyncService;
import com.blog.task.domain.TaskEntity;
import com.blog.task.service.TaskService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * @Description 测试接口
 * @Author lxk
 * @CreateTime 2025-06-10
 */

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private TaskService taskService;

    @GetMapping("/get")
    public Result getTest() {
        System.out.println("测试方法调用");
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setClazz(NettyFileSyncService.class);

        taskEntity.setMethodName("clearTempFileOrPath");
        taskEntity.setParams(new Object[]{"测试参数名称"});
        Class<?>[] paramTypes = new Class<?>[]{String.class};
        taskEntity.setParamsClazz(paramTypes);
        taskEntity.setTime("5s");
        taskEntity.setCount(1);
        taskService.createTask(taskEntity);
        return ResultFactory.buildSuccessResult();
    }
}
