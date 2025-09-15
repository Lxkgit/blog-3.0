package com.blog.pi.netty.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.dto.NettyResponse;
import com.blog.pi.netty.enums.SensorTypeEnum;
import com.blog.pi.netty.service.vo.SensorCommandVo;
import com.blog.pi.netty.service.vo.SteeringEngineVo;
import com.blog.pi.netty.thread.CommandSendThread;
import com.blog.pi.netty.thread.CommandThreadService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: 传感器控制服务类
 * @Author: lxk
 * @date 2024/3/29 19:56
 */

@Component
public class SensorControlService {

    @Resource
    private NettyClient nettyClient;

    public void sendCommand(String command, String requestId) {
        JSONObject jsonObject = JSONObject.parseObject(command);
        List<SensorCommandVo> sensorCommandVoList = new ArrayList<>();
        if (SensorTypeEnum.DUO_JI.getSensorCode().equals(jsonObject.get("sensorType"))) {
            JSONArray jsonArray = JSONArray.parse(jsonObject.get("commandList").toString());
            for (int i=0; i<jsonArray.size(); i++) {
                SteeringEngineVo steeringEngineVo = new SteeringEngineVo();
                steeringEngineVo.setSensorType(jsonObject.getString("sensorType"));
                steeringEngineVo.setChipType(jsonObject.getString("chipType"));
                JSONObject data = JSONObject.parseObject(jsonArray.getString(i));
                steeringEngineVo.setControlIntervalTime(data.getInteger("controlIntervalTime"));
                steeringEngineVo.setData(data.getInteger("data"));
                sensorCommandVoList.add(steeringEngineVo);
            }
        }

        if (!sensorCommandVoList.isEmpty()) {
            CommandSendThread commandThread = new CommandSendThread(sensorCommandVoList);
            CommandThreadService.commandSendPool.execute(commandThread);
        }

        // 响应服务端处理结果
        NettyResponse nettyResponse = new NettyResponse(true);
        NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(requestId, "",nettyResponse);
        nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);

    }
}
