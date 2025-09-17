package com.blog.file.controller;

import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.file.service.TaskService;
import com.blog.task.domain.TaskEntity;
import jakarta.annotation.Resource;
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

    /**
     * 查询主任务列表
     *
     * @return
     */
    @GetMapping("/select/list")
    public Result selectTaskBaseList() {
        return ResultFactory.buildSuccessResult(taskService.selectTaskBaseList());
    }

    /**
     * 查询子任务
     *
     * @param taskParamVo
     * @return
     */
    @GetMapping("/select/id")
    public Result selectTaskEntityById(@RequestBody TaskParamVo taskParamVo) {
        return ResultFactory.buildSuccessResult(taskService.selectTaskEntityById(taskParamVo));
    }

    @PostMapping("/update")
    public Result updateTask(@RequestBody TaskParamVo taskParamVo) {
        taskService.updateTask(taskParamVo);
        return ResultFactory.buildSuccessResult();
    }


    @GetMapping("/log/select/list")
    public Result selectTaskLogList(TaskLogVo taskLogVo) {
        return ResultFactory.buildSuccessResult(taskService.selectTaskLogList(taskLogVo));
    }

}
