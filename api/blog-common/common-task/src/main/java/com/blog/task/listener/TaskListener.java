package com.blog.task.listener;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.blog.core.domain.common.MsgHead;
import com.blog.core.domain.common.TaskMsgHead;
import com.blog.core.domain.file.task.entity.TaskLog;
import com.blog.redis.service.RedisService;
import com.blog.task.config.SpringContextHolder;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
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
    private CreateTaskService createTaskService;

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
                            TaskEntity taskEntity = (TaskEntity) tuple.getValue();

                            if (taskEntity != null) {
                                // 1. 提前获取所有必要值（避免线程异步问题）
                                Class<?> targetClass = taskEntity.getClazz();
                                String methodName = taskEntity.getMethodName();
                                List<Class<?>> paramTypes = CollectionUtils.isEmpty(taskEntity.getParamsClazz()) ? new ArrayList<>() : taskEntity.getParamsClazz(); // 改为数组
                                List<Object> methodParams = CollectionUtils.isEmpty(taskEntity.getTaskParams()) ? new ArrayList<>() : taskEntity.getTaskParams();

                                // 定时任务添加任务执行头数据
                                paramTypes.add(MsgHead.class);
                                TaskMsgHead taskMsgHead = new TaskMsgHead();
                                taskMsgHead.setTaskCode(taskEntity.getTaskCode());
                                taskMsgHead.setChildTaskCode(taskEntity.getChildTaskCode());
                                String taskUUID = UUID.randomUUID().toString().replace("-", "");
                                taskMsgHead.setTaskUUID(taskUUID);
                                methodParams.add(MsgHead.buildTaskMsgHead(taskMsgHead));

                                // 2. 获取目标类的实例
                                Object targetInstance;
                                try {
                                    // 尝试从Spring容器获取Bean
                                    targetInstance = SpringContextHolder.getBean(targetClass);
                                } catch (Exception e) {
                                    // 如果获取失败，说明该类不是Spring管理的Bean
                                    logger.warn("类 {} 不是Spring Bean，将使用反射创建实例", targetClass.getName());
                                    targetInstance = targetClass.getDeclaredConstructor().newInstance();
                                }

                                // 3. 查找方法
                                Method method = ReflectionUtils.findMethod(targetClass, methodName, paramTypes.toArray(new Class[0]));
                                if (method == null) {
                                    logger.error("类:{} 中不存在:{} 方法", targetClass, methodName);
                                    throw new IllegalArgumentException();
                                }

                                // 4. 设置方法可访问
                                method.setAccessible(true);

                                // 5. 提交任务（捕获所有必要变量）
                                logger.info("执行任务：{} method:{}", targetClass, methodName);
                                taskEntity.setIndexCount(taskEntity.getIndexCount() + 1);
                                Object finalTargetInstance = targetInstance;
                                baseTaskThread.execute(() -> {
                                    TaskLog taskLog = new TaskLog();
                                    taskLog.setTaskName(taskEntity.getTaskName());
                                    taskLog.setTaskCode(taskEntity.getTaskCode());
                                    taskLog.setChildTaskCode(taskEntity.getChildTaskCode());
                                    taskLog.setTaskUUID(taskUUID);
                                    taskLog.setTaskLogType(1);
                                    taskLog.setIndexCount(taskEntity.getIndexCount());
                                    taskLog.setTaskCount(taskEntity.getTaskCount());
                                    taskLog.setStartTime(new Date());
                                    try {
                                        // 使用正确的目标实例
                                        Object result = ReflectionUtils.invokeMethod(method, finalTargetInstance, methodParams.toArray());
                                        logger.info("方法执行结果: {}", result);
                                        if (result != null) {
                                            taskLog.setTaskResult(result.toString());
                                        }
                                    } catch (Exception e) {
                                        logger.error("方法执行失败", e);
                                        taskLog.setErrorMsg(e.getMessage());
                                        // 添加错误处理逻辑
                                    } finally {
                                        // 创建任务启动执行日志
                                        redisService.setList(TaskConstant.TASK_LOG, taskLog);
                                    }
                                });

                                if (taskEntity.getTaskCount() == -1 || taskEntity.getIndexCount() < taskEntity.getTaskCount()) {
                                    // 创建下一次任务
                                    createTaskService.createTask(taskEntity);
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
