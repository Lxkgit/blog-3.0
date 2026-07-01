package com.blog.timer.action;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Component
public class TimerActionManager {

//    private final Map<String, TimerAction> actionMap;
//
//    public TimerActionManager(Map<String, TimerAction> actionMap) {
//        this.actionMap = actionMap;
//    }

    private final Map<String, TimerAction> actionMap = new ConcurrentHashMap<>();

    public TimerActionManager(List<TimerAction> actions) {
        for (TimerAction action : actions) {
            actionMap.put(action.getCode(), action);
        }
    }

    public boolean checkCode(String code) {
        return actionMap.containsKey(code);
    }

    public TimerAction get(String code) {
        return actionMap.get(code);
    }

    public Map<String, TimerAction> getAllActions() {
        return actionMap;
    }
}
