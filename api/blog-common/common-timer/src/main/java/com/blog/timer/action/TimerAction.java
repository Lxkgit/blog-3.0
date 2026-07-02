package com.blog.timer.action;

import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTask;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

public interface TimerAction {

    String getCode();

    String getName();

    String getParamTemplate();

    void checkParam(TimerAction action, String json);

    void beforeExecute(TimerTask timerTask);

    void execute(TimerTask task);

    void afterExecute(TimerTask timerTask);

    void finalExecute(TimerTask timerTask);
}
