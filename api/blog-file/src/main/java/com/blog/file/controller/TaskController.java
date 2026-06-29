package com.blog.file.controller;


import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.core.result.Result;
import com.blog.core.result.ResultFactory;
import com.blog.core.utils.SecurityUtil;
import com.blog.file.service.TaskService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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
     * 创建任务
     *
     * @param taskParamVo
     * @return
     */
    @PostMapping("/insert")
    public Result insertTask(@RequestBody TaskParamVo taskParamVo) {
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
    public Result updateTask(@RequestBody TaskParamVo taskParamVo) {
        taskService.updateTask(taskParamVo);
        return ResultFactory.buildSuccessResult();
    }

    @PostMapping("/select/list")
    public Result selectTaskList(@RequestBody TaskParamVo taskParamVo) {

        return ResultFactory.buildSuccessResult(taskService.selectTaskList(taskParamVo));
    }

    /**
     * 查询主任务列表
     *
     * @return
     */
    @GetMapping("/select/base/list")
    public Result selectTaskBaseList() {
        return ResultFactory.buildSuccessResult(taskService.selectTaskBaseList());
    }

    /**
     * 查询子任务
     *
     * @param taskCode
     * @return
     */
    @GetMapping("/select/child/id")
    public Result selectTaskEntityById(@RequestParam("taskCode") String taskCode) {
        return ResultFactory.buildSuccessResult(taskService.selectTaskEntityById(taskCode));
    }

    /**
     * 立即执行任务
     *
     * @return
     */
    @GetMapping("/start")
    public Result startTask(@RequestParam(value = "childTaskCode") String childTaskCode) {
        taskService.startTask(childTaskCode);
        return ResultFactory.buildSuccessResult();
    }

//    /**
//     * 查询任务执行日志
//     *
//     * @param taskLogVo
//     * @return
//     */
//    @GetMapping("/log/select/list")
//    public Result selectTaskLogList(TaskLogVo taskLogVo) {
//        return ResultFactory.buildSuccessResult(taskService.selectTaskLogList(taskLogVo));
//    }

    /**
     * 查询任务执行日志
     *
     * @param taskUUID
     * @return
     */
    @GetMapping("/log/select/id")
    public Result selectTaskLogByTaskUUID(@RequestParam("taskUUID") String taskUUID) {
        return ResultFactory.buildSuccessResult(taskService.selectTaskLogByTaskUUID(taskUUID));
    }

}
