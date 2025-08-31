package com.blog.file.controller;

import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.InsertGroup;
import com.blog.file.service.TaskService;
import com.blog.task.domain.TaskEntity;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Description 任务功能接口
 * @Author lxk
 * @CreateTime 2025-08-22
 */

@RestController
@RequestMapping("/task")
public class TaskController {

    @Resource
    private TaskService taskService;

    @PostMapping("/update")
    public Result updateTask(@RequestBody TaskEntity entity) {
       taskService.updateTask(entity);
        return ResultFactory.buildSuccessResult();
    }

    @GetMapping("/select/list")
    public Result selectTaskList() {
        return ResultFactory.buildSuccessResult(taskService.selectTaskInfoList());
    }

    @GetMapping("/log/select/list")
    public Result selectTaskLogList(TaskLogVo taskLogVo) {
        return ResultFactory.buildSuccessResult(taskService.selectTaskLogList(taskLogVo));
    }

}
