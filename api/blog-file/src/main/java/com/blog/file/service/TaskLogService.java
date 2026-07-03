package com.blog.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.result.ResultPage;
import com.blog.timer.entity.TimerTask;

import java.util.Date;

/**
 * @author lxk
 * @description 定时任务日志服务类
 * @date 2026/07/03
 */

public interface TaskLogService {

    ResultPage<TaskLogVo> selectTaskLogList(TaskLogVo taskLogVo);

    ResultPage<TaskLog> selectTaskLogByUuid(TaskLogVo taskLogVo);

    void registerTaskLog(TimerTask timerTask);

    Integer executeTaskLog(MsgHead msgHead, TaskLog taskLog);

    void taskStartLog(TimerTask timerTask);

    void taskStartLog(Integer id);

    void taskSuccessLog(TimerTask timerTask, String result);

    void taskSuccessLog(Integer id, String result);

    void taskFailureLog(TimerTask timerTask, Exception e);

    void taskFailureLog(Integer id, Exception e);

    void taskEndLog(TimerTask timerTask);

    void taskEndLog(Integer id);






}
