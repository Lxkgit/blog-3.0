package com.blog.task.listener;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.blog.redis.service.RedisService;
import com.blog.task.config.TaskThread;
import com.blog.task.constant.TaskConstant;
import com.blog.task.domain.TaskEntity;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @Description
 * @Author lxk
 * @CreateTime 2025-07-31
 */

@Component
public class TaskListener implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(TaskListener.class);

    @Resource
    private RedisService redisService;

    @Resource
    private Executor baseTaskThread;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("任务定时监控线程启动");
        baseTaskThread.execute(this::taskListenerThread);
    }

    private void taskListenerThread() {

        while (true) {
            try {
                Long taskCount = redisService.getZSetSize(TaskConstant.TASK_QUEUE);
                if (taskCount != 0L) {
                    ZSetOperations.TypedTuple<Object> tuple = redisService.getZSetByIndex(TaskConstant.TASK_QUEUE, 0);
                    if (tuple != null) {
                        // 任务执行时间
                        double score = tuple.getScore();
                        long taskTime = (long) score;

                        // 当前时间
                        LocalDateTime now = LocalDateTime.now();
                        ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault());
                        long newTime = zonedDateTime.toEpochSecond();

                        if (newTime >= taskTime) {
                            logger.info("开始执行任务");
                            redisService.removeZSetByIndex(TaskConstant.TASK_QUEUE, 0);
                            TaskEntity taskEntity = JSONObject.parseObject(Objects.requireNonNull(tuple.getValue()).toString(), TaskEntity.class, JSONReader.Feature.SupportClassForName);

                            if (taskEntity != null) {
                                // 1. 提前获取所有必要值（避免线程异步问题）
                                Class<?> targetClass = taskEntity.getClazz();
                                String methodName = taskEntity.getMethodName();
                                Class<?>[] paramTypes = taskEntity.getParamsClazz(); // 改为数组
                                Object[] methodParams = taskEntity.getParams();

                                // 2. 获取目标类的实例（关键修复！）
                                // 方式1：如果目标类有默认构造（简单场景）
                                Object targetInstance = targetClass.getDeclaredConstructor().newInstance();

                                // 方式2：更推荐 - 从Spring容器获取Bean（确保依赖注入）
                                // Object targetInstance = applicationContext.getBean(targetClass);

                                // 3. 查找方法
                                Method method = ReflectionUtils.findMethod(targetClass, methodName, paramTypes);
                                if (method == null) {
                                    throw new IllegalArgumentException("Method not found: " + methodName);
                                }

                                // 4. 设置方法可访问
                                method.setAccessible(true);

                                logger.info("执行任务：class:{} method:{}", targetClass, methodName);

                                // 5. 提交任务（捕获所有必要变量）
                                baseTaskThread.execute(() -> {
                                    try {
                                        // 使用正确的目标实例
                                        Object result = ReflectionUtils.invokeMethod(method, targetInstance, methodParams);
                                        logger.info("方法执行结果: {}", result);
                                    } catch (Exception e) {
                                        logger.error("方法执行失败", e);
                                        // 添加错误处理逻辑
                                    }
                                });
                            }
                        }
                    }
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("任务调用异常：{}", e.getMessage(), e);

            }
        }
    }
}
