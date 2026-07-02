package com.blog.file.controller;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.task.BlogDateSyncTaskAction;
import com.blog.timer.action.TimerActionManager;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTaskDefinition;
import com.blog.timer.entity.policy.Policy;
import com.blog.timer.entity.trigger.CronTrigger;
import com.blog.timer.manager.TimerManager;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

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
    private TimerActionManager timerActionManager;

    @GetMapping("/get")
    public Result getTest() {

        TimerTaskContext context2 = new TimerTaskContext();
        context2.put("id", 2);
        context2.put("name", "任务2");
        TimerTaskDefinition definition2 = TimerTaskDefinition.builder()
                .taskCode(timerTask.getCode())
                .action(timerTask)
                .context(context2)
                .policy(new Policy(4))
                .trigger(new CronTrigger("0 50 * * * *"))
                .build();

//        timerManager.schedule(definition2);

//        return ResultFactory.buildSuccessResult(timerTaskRegistry.list());

//        return ResultFactory.buildSuccessResult(timerActionManager.getAllActions());
        return ResultFactory.buildSuccessResult(timerManager.schedule(definition2).getTaskId());
    }

    @GetMapping("/list")
    public Result getTaskList() {

        ArrayList<Object> list =  new ArrayList<>(timerManager.getAllTask());
        return ResultFactory.buildSuccessResult(list);
    }

    @PostMapping("/todo")
    public Result postTest(@RequestBody JSONObject json) {

        timerManager.executeNow(json.get("taskId").toString());

        return ResultFactory.buildSuccessResult("执行完成");
    }
}
