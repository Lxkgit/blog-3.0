package com.blog.timer.entity.policy;

import lombok.Data;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2026-06-25
 */

@Data
public class Policy {

    /**
     * 最大执行次数。
     * null 表示无限循环
     */
    private final Integer executeCount;

    public Policy(Integer executeCount) {

        if (executeCount != null && executeCount <= 0) {
            throw new IllegalArgumentException("executeCount 必须大于0");
        }

        this.executeCount = executeCount;
    }

    /**
     * 是否继续执行。
     *
     * @param currentCount 当前已执行次数
     * @return true 继续执行
     */
    public boolean shouldContinue(int currentCount) {
        if (executeCount == null) {
            return true;
        }
        return currentCount < executeCount;
    }

    public Integer getExecuteCount() {
        return executeCount;
    }

}
