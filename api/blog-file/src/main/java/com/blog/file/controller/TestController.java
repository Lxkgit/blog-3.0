package com.blog.file.controller;

import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.task.BlogDateSyncTaskAction;
import com.blog.timer.action.TimerActionManager;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTaskDefinition;
import com.blog.timer.entity.policy.Policy;
import com.blog.timer.entity.trigger.DelayTrigger;
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

        TimerTaskContext context1 = new TimerTaskContext();
        context1.put("id", 1);
        context1.put("name", "任务1");

        TimerTaskDefinition definition1 = TimerTaskDefinition.builder()
                .taskCode(timerTask.getCode())
                .action(timerTask)
                .context(context1)
                .policy(new Policy(3))
                .trigger(new DelayTrigger(Duration.ofSeconds(4)))
                .build();

        TimerTaskContext context2 = new TimerTaskContext();
        context2.put("id", 2);
        context2.put("name", "任务2");
        TimerTaskDefinition definition2 = TimerTaskDefinition.builder()
                .taskCode(timerTask.getCode())
                .action(timerTask)
                .context(context2)
                .policy(new Policy(4))
                .trigger(new DelayTrigger(Duration.ofSeconds(5)))
                .build();

        timerManager.schedule(definition1);
        timerManager.schedule(definition2);

//        return ResultFactory.buildSuccessResult(timerTaskRegistry.list());

        return ResultFactory.buildSuccessResult(timerActionManager.getAllActions());
    }
}
