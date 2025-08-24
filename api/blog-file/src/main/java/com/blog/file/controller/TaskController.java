package com.blog.file.controller;

import com.blog.core.domain.file.task.vo.TaskInfoVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.InsertGroup;
import com.blog.file.service.TaskService;
import jakarta.annotation.Resource;
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

    /**
     * 创建传感器
     *
     */
    @PostMapping("/insert")
//    @PreAuthorize("hasAnyAuthority('sys:sensor:save')")
    public Result insertTask(@Validated(value = {InsertGroup.class}) @RequestBody TaskInfoVo taskInfoVo) {
        taskService.insertTask(taskInfoVo);
        return ResultFactory.buildSuccessResult();
    }

    @DeleteMapping("/delete")
    public Result deleteTask(@RequestParam(value = "id") Integer id) {
        taskService.deleteTask(id);
        return ResultFactory.buildSuccessResult();
    }

    @PostMapping("/update")
    public Result updateTask(@RequestBody TaskInfoVo taskInfoVo) {
        taskService.updateTask(taskInfoVo);
        return ResultFactory.buildSuccessResult();
    }

    @GetMapping("/select/list")
    public Result selectTaskList() {
        return ResultFactory.buildSuccessResult(taskService.selectTaskInfoList());
    }

    @GetMapping("/log/select/list")
    public Result selectTaskLogList() {
        return ResultFactory.buildSuccessResult(taskService.selectTaskLogList());
    }

}
