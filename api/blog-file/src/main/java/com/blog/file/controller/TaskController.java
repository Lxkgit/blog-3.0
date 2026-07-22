package com.blog.file.controller;


import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.valication.group.InsertGroup;
import com.blog.core.valication.group.SelectListGroup;
import com.blog.core.valication.group.UpdateGroup;
import com.blog.file.service.TaskLogService;
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

    @Resource
    private TaskLogService taskLogService;

    /**
     * 创建任务
     *
     * @param taskParamVo
     * @return
     */
    @PostMapping("/insert")
    public Result insertTask(@RequestBody @Validated(value = {InsertGroup.class}) TaskParamVo taskParamVo) {
        taskService.insertTask(taskParamVo);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 删除任务
     *
     * @param id
     * @return
     */
    @DeleteMapping("/delete")
    public Result deleteTask(@RequestParam(value = "id") Integer id) {
        taskService.deleteTask(id);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 修改任务
     *
     * @param taskParamVo
     * @return
     */
    @PostMapping("/update")
    public Result updateTask(@RequestBody @Validated(value = {UpdateGroup.class}) TaskParamVo taskParamVo) {
        taskService.updateTask(taskParamVo);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 查看任务列表
     *
     * @param taskParamVo
     * @return
     */
    @PostMapping("/select/list")
    public Result selectTaskList(@RequestBody TaskParamVo taskParamVo) {
        return ResultFactory.buildSuccessResult(taskService.selectTaskList(taskParamVo));
    }

    /**
     * 操作任务参数配置-启用任务
     *
     * @param id
     * @return
     */
    @GetMapping("/start")
    public Result startTask(@RequestParam(value = "id") Integer id) {
        taskService.startTask(id);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 操作任务参数配置-停止任务
     *
     * @param id
     * @return
     */
    @GetMapping("/stop")
    public Result stopTask(@RequestParam(value = "id") Integer id) {
        taskService.stopTask(id);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 操作执行队列中的任务-立即执行任务
     *
     * @return
     */
    @GetMapping("/running")
    public Result runningTask(@RequestParam(value = "id") Integer id) {
        taskService.runningTask(id);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 操作执行队列中的任务-取消执行任务
     *
     * @return
     */
    @GetMapping("/cancel")
    public Result cancelTask(@RequestParam(value = "id") Integer id) {
        taskService.cancelTask(id);
        return ResultFactory.buildSuccessResult();
    }

    /**
     * 查询全部基础任务
     *
     * @return
     */
    @GetMapping("/select/base")
    public Result selectTaskBaseList() {
        return ResultFactory.buildSuccessResult(taskService.selectBaseTaskList());
    }

    /**
     * 查询运行中的任务
     *
     * @return
     */
    @GetMapping("/select/running")
    public Result selectRunningTask(@Validated(value = {SelectListGroup.class}) TaskParamVo taskParamVo) {
        return ResultFactory.buildSuccessResult(taskService.selectRunningTask(taskParamVo));
    }


    /**
     * 查询任务执行日志
     *
     * @param taskLogVo
     * @return
     */
    @GetMapping("/log/select/list")
    public Result selectTaskLogList(TaskLogVo taskLogVo) {
        return ResultFactory.buildSuccessResult(taskLogService.selectTaskLogList(taskLogVo));
    }

    /**
     * 查询任务执行日志
     *
     * @param taskLogVo
     * @return
     */
    @GetMapping("/log/select/id")
    public Result selectTaskLogByUuid(TaskLogVo taskLogVo) {
        return ResultFactory.buildSuccessResult(taskLogService.selectTaskLogByUuid(taskLogVo));
    }

}
