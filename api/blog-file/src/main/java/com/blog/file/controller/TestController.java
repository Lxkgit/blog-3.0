package com.blog.file.controller;

import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.task.BlogDateSyncTaskAction;
import com.blog.timer.action.TimerActionManager;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.handle.TimerHandle;
import com.blog.timer.manager.TimerManager;
import com.blog.timer.registry.TimerTaskRegistry;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Description 测试接口
 * @Author lxk
 * @CreateTime 2025-06-10
 */

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private BlogDateSyncTaskAction timerTask;

    @Resource
    private TimerManager timerManager;

    @Resource
    private TimerTaskRegistry timerTaskRegistry;

    @Resource
    private TimerActionManager timerActionManager;

    @GetMapping("/get")
    public Result getTest() {

//        TimerTaskContext context = new TimerTaskContext();
//        context.put("id", 1);
//        context.put("name", "测试");
//
//        timerManager.schedule(Duration.ofSeconds(10), timerTask.getCode(), timerTask, context);
//        LocalDateTime time = LocalDate.now().atTime(16, 59);
//        TimerHandle handle = timerManager.schedule(time, timerTask.getCode(), timerTask, context);
//        timerManager.schedule("0 59 16 * * ?", timerTask.getCode(), timerTask, context);
//
//
//        return ResultFactory.buildSuccessResult(timerTaskRegistry.list());

        return ResultFactory.buildSuccessResult(timerActionManager.getAllActions());
    }
}
