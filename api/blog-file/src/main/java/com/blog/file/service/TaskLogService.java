package com.blog.file.service;

import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.core.domain.file.task.vo.TaskLogVo;
import com.blog.core.result.ResultPage;
import com.blog.timer.entity.TimerTask;

/**
 * @author lxk
 * @description 定时任务日志服务类
 * @date 2026/07/03
 */

public interface TaskLogService {

    /**
     * 查询日志列表
     *
     * @param taskLogVo 查询参数
     * @return 返回数据
     */
    ResultPage<TaskLogVo> selectTaskLogList(TaskLogVo taskLogVo);

    /**
     * 根据uuid查询日志详情
     *
     * @param taskLogVo 查询参数
     * @return 返回数据
     */
    ResultPage<TaskLog> selectTaskLogByUuid(TaskLogVo taskLogVo);

    /**
     * 创建任务时生成注册日志
     *
     * @param timerTask 定时任务参数
     */
    void registerTaskLog(TimerTask timerTask);

    /**
     * 响应任务日志-注册 （日志会有后续多次记录）
     *
     * @param msgHead 任务头
     * @param taskLog 日志详情
     */
    void executeTaskLog(MsgHead msgHead, TaskLog taskLog);

    /**
     * 创建任务开始日志
     *
     * @param timerTask 任务参数
     */
    void taskStartLog(TimerTask timerTask);

    /**
     * 响应任务日志-开始
     *
     * @param id 日志id
     */
    void taskStartLog(Integer id);

    /**
     * 创建任务成功日志
     *
     * @param timerTask 任务参数
     * @param result    任务执行返回数据
     */
    void taskSuccessLog(TimerTask timerTask, String result);

    /**
     * 响应任务日志-成功
     *
     * @param id     日志id
     * @param result 日志响应内容
     */
    void taskSuccessLog(Integer id, String result);

    /**
     * 创建任务失败日志
     *
     * @param timerTask 任务参数
     * @param e         异常信息
     */
    void taskFailureLog(TimerTask timerTask, Exception e);

    /**
     * 响应任务日志-失败
     *
     * @param id 日志id
     * @param e  异常信息
     */
    void taskFailureLog(Integer id, Exception e);

    /**
     * 创建任务结束日志
     *
     * @param timerTask 任务参数
     */
    void taskEndLog(TimerTask timerTask);

    /**
     * 创建任务结束日志
     *
     * @param taskUuid 定时任务uuid
     */
    void taskEndLog(String taskUuid);

    /**
     * 响应任务日志-结束
     *
     * @param id 日志id
     */
    void taskEndLog(Integer id);

    /**
     * 完整任务日志
     * @param taskLog 日志内容
     */
    void completeTaskLog(TaskLog taskLog);

}
