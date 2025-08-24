package com.blog.task.listener;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.redis.service.RedisService;
import com.blog.task.constant.TaskConstant;
import com.blog.task.domain.TaskEntity;
import com.blog.task.service.CreateTaskService;
import jakarta.annotation.Resource;
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

    @Resource
    private CreateTaskService taskService;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("任务定时监控线程启动");
        baseTaskThread.execute(this::taskListenerThread);
    }

    // 忽略无限循环与忙等待报错
    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    private void taskListenerThread() {
        while (true) {
            try {
                Long taskCount = redisService.getZSetSize(TaskConstant.TASK_QUEUE);
                if (taskCount != 0L) {
                    ZSetOperations.TypedTuple<Object> tuple = redisService.getZSetByIndex(TaskConstant.TASK_QUEUE, 0);
                    if (tuple != null) {
                        // 任务执行时间
                        Double score = tuple.getScore();
                        assert score != null;
                        long taskTime = score.longValue();

                        // 当前时间
                        LocalDateTime now = LocalDateTime.now();
                        ZonedDateTime zonedDateTime = now.atZone(ZoneId.systemDefault());
                        long newTime = zonedDateTime.toEpochSecond();

                        if (newTime >= taskTime) {
                            redisService.removeZSetByIndex(TaskConstant.TASK_QUEUE, 0);
                            TaskEntity taskEntity = JSONObject.parseObject(Objects.requireNonNull(tuple.getValue()).toString(), TaskEntity.class, JSONReader.Feature.SupportClassForName);

                            if (taskEntity != null) {
                                // 1. 提前获取所有必要值（避免线程异步问题）
                                Class<?> targetClass = taskEntity.getClazz();
                                String methodName = taskEntity.getMethodName();
                                Class<?>[] paramTypes = taskEntity.getParamsClazz(); // 改为数组
                                Object[] methodParams = taskEntity.getParams();

                                // 2. 获取目标类的实例
                                Object targetInstance = targetClass.getDeclaredConstructor().newInstance();

                                // 3. 查找方法
                                Method method = ReflectionUtils.findMethod(targetClass, methodName, paramTypes);
                                if (method == null) {
                                    logger.error("类:{} 中不存在:{} 方法", targetClass, methodName);
                                    throw new IllegalArgumentException();
                                }

                                // 4. 设置方法可访问
                                method.setAccessible(true);

                                // 5. 提交任务（捕获所有必要变量）
                                logger.info("执行任务：{} method:{}", targetClass, methodName);
                                taskEntity.setIndexCount(taskEntity.getIndexCount() + 1);
                                baseTaskThread.execute(() -> {
                                    TaskLog taskLog = new TaskLog();
                                    taskLog.setTaskUUID(taskEntity.getTaskUUID());
                                    taskLog.setIndexCount(taskEntity.getIndexCount());
                                    try {
                                        // 使用正确的目标实例
                                        Object result = ReflectionUtils.invokeMethod(method, targetInstance, methodParams);
                                        logger.info("方法执行结果: {}", result);
                                        if (result != null) {
                                            taskLog.setTaskResult(result.toString());
                                        }
                                    } catch (Exception e) {
                                        logger.error("方法执行失败", e);
                                        taskLog.setErrorMsg(e.getMessage());
                                        // 添加错误处理逻辑
                                    } finally {
                                        redisService.setList(TaskConstant.TASK_LOG, JSONObject.toJSONString(taskLog));
                                    }
                                });
                                if (taskEntity.getIndexCount() < taskEntity.getCount() || taskEntity.getCount() == -1) {
                                    taskService.createTask(taskEntity);
                                }
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
