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

        if (executeCount != null && executeCount <= -1) {
            throw new IllegalArgumentException("executeCount 必须大于等于 -1");
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
        // 执行次数为 -1 表示无限执行
        if (executeCount == -1) {
            return true;
        }
        return currentCount < executeCount;
    }

    public Integer getExecuteCount() {
        return executeCount;
    }

}
