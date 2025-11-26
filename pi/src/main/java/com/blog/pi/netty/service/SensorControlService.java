package com.blog.pi.netty.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.domain.common.MsgHead;
import com.blog.pi.mqtt.service.ChipMsgService;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.dto.NettyResponse;
import com.blog.pi.netty.enums.SensorTypeEnum;
import com.blog.pi.netty.service.vo.SensorCommandVo;
import com.blog.pi.netty.service.vo.SteeringEngineVo;
//import com.blog.pi.netty.thread.CommandSendThread;
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

    @Resource
    private ChipMsgService chipMsgService;

    public void sendCommand(String command, String requestId, MsgHead msgHead) {
        JSONObject jsonObject = JSONObject.parseObject(command);

        String chipCommand = chipMsgService.nettyDataToCommand(jsonObject);

        // 响应服务端处理结果
        NettyResponse nettyResponse = new NettyResponse(true);
        NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(msgHead, nettyResponse);
        nettyPacket.setMsgHead(msgHead);
        nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);

    }
}
