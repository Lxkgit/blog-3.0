package com.blog.file.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.core.domain.file.device.entity.UserDevice;
import com.blog.core.domain.file.task.vo.TaskParamVo;
import com.blog.core.domain.netty.dto.NettyPacket;
import com.blog.core.domain.netty.dto.file.NettySyncFileDto;
import com.blog.core.domain.netty.enums.NettyTopic;
import com.blog.core.domain.netty.head.MsgHead;
import com.blog.core.domain.netty.head.TaskMsgHead;
import com.blog.core.domain.file.task.entity.TaskParam;
import com.blog.core.domain.file.task.entity.TaskUuid;
import com.blog.file.mapper.TaskParamMapper;
import com.blog.file.mapper.TaskUuidMapper;
import com.blog.file.mapper.UserDeviceMapper;
import com.blog.file.netty.service.NettyServer;
import com.blog.file.service.TaskLogService;
import com.blog.timer.action.TimerAction;
import com.blog.timer.context.TimerTaskContext;
import com.blog.timer.entity.TimerTask;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Description 系统定时任务类
 * @Author lxk
 * @CreateTime 2026-07-01
 */


public abstract class SystemTimerAction implements TimerAction {

    private static final Logger logger = LoggerFactory.getLogger(SystemTimerAction.class);

    @Resource
    private TaskUuidMapper taskUuidMapper;

    @Resource
    private TaskLogService taskLogService;

    @Resource
    private TaskParamMapper taskParamMapper;

    @Resource
    private UserDeviceMapper userDeviceMapper;

    @Resource
    private NettyServer nettyServer;

    @Override
    public void checkParam(TimerAction action, String json) {
        logger.info("开始校验参数: {}", action.getParamTemplate());
        TaskParamVo taskParamVo = JSONObject.parseObject(json, TaskParamVo.class);
        doCheckParam(action, taskParamVo.getParamJson());
    }

    protected abstract void doCheckParam(TimerAction action, String param);

    @Override
    public void beforeExecute(TimerTask timerTask) {
        // 记录任务 uuid 于 任务参数表id 映射
        TaskUuid taskUuid = new TaskUuid();
        taskUuid.setTaskId(timerTask.getDefinition().getId());
        taskUuid.setUuid(timerTask.getUuid());
        taskUuidMapper.insert(taskUuid);
    }

    @Override
    public void execute(TimerTask timerTask)  {
        try {
            taskLogService.registerTaskLog(timerTask);
            taskLogService.taskStartLog(timerTask);
            TaskMsgHead taskMsgHead = TaskMsgHead.builder()
                    .userId(timerTask.getDefinition().getUserId())
                    .taskUuid(timerTask.getUuid())
                    .taskCode(timerTask.getDefinition().getAction().getCode())
                    .build();

            String result = doExecute(timerTask.getDefinition().getContext(), taskMsgHead);
            taskLogService.taskSuccessLog(timerTask, result);
        } catch (Exception e) {
            taskLogService.taskFailureLog(timerTask, e);
            logger.error("定时任务 {} 执行异常: uuid: {} 异常信息: {}",
                    timerTask.getDefinition().getAction().getName(), timerTask.getUuid(), e.getMessage(), e);
        } finally {
            taskLogService.taskEndLog(timerTask);
        }
    }

    protected abstract String doExecute(TimerTaskContext context, TaskMsgHead taskMsgHead);

    @Override
    public void afterExecute(TimerTask timerTask) {
        LambdaQueryWrapper<TaskUuid> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskUuid::getUuid, timerTask.getUuid());
        taskUuidMapper.delete(wrapper);
    }

    @Override
    public void finalExecute(TimerTask timerTask) {
        Integer taskId = timerTask.getDefinition().getId();

        TaskParam param = TaskParam.builder().id(taskId).taskStatus("2").build();
        taskParamMapper.updateById(param);

    }

    /**
     * 发送文件同步消息至树莓派
     *
     * @param nettySyncFileDto 同步文件参数
     */
    protected boolean sendSyncFileMsg(MsgHead msgHead, NettySyncFileDto nettySyncFileDto, Integer userId) {

        // 获取用户默认同步数据设备
        LambdaQueryWrapper<UserDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDevice::getUserId, userId);
        List<UserDevice> deviceList = userDeviceMapper.selectList(wrapper);

        if (CollectionUtils.isNotEmpty(deviceList)) {
            UserDevice device = deviceList.get(0);
            String registerId = device.getDeviceCode();

            NettyPacket<NettySyncFileDto> nettyPacket = NettyPacket.buildRequest(NettyTopic.BLOG_FILE_SYNC, nettySyncFileDto);
            nettyPacket.setMsgHead(msgHead);
            return nettyServer.sendByRegisterIdLimitTime(registerId, nettyPacket.getMsgHead().getNettyMsgHead().getRequestId(),
                    JSON.toJSONString(nettyPacket), 2 * 60);

        }
        return false;
    }
}
