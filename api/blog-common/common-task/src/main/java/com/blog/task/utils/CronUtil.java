package com.blog.task.utils;


import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-31
 */

public class CronUtil {

    /**
     * 获取cron表达式下次执行时间时间戳(时间戳单位为秒)
     *
     * @param cronStr cron表达式
     */
    public static long getCronNextTimeEpoch(String cronStr) {

        // 创建解析器
        CronDefinition def = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
        CronParser parser = new CronParser(def);

        // 解析表达式
        Cron cron = parser.parse(cronStr);

        // 创建执行时间计算器
        ExecutionTime execTime = ExecutionTime.forCron(cron);

        // 获取当前时间
        ZonedDateTime now = ZonedDateTime.now();

        // 计算下次执行时间
        Optional<ZonedDateTime> next = execTime.nextExecution(now);

        return next.map(zonedDateTime -> zonedDateTime.toInstant().getEpochSecond()).orElse(0L);
    }
}
