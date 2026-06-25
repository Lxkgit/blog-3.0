package com.blog.timer.action;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class TimerActionManager {

    private final Map<String, TimerAction> actionMap;

    public TimerActionManager(Map<String, TimerAction> actionMap) {
        this.actionMap = actionMap;
    }

    public Map<String, TimerAction> getAllActions() {
        return actionMap;
    }
}
