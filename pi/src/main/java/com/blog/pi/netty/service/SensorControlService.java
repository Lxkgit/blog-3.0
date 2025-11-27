package com.blog.pi.netty.service;

import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.domain.common.MsgHead;
import com.blog.pi.mqtt.MqttService;
import com.blog.pi.mqtt.enums.MQTTTopicEnum;
import com.blog.pi.mqtt.service.ChipMsgService;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.dto.NettyResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


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

    @Resource
    private MqttService mqttService;

    public void sendCommand(String command, String requestId, MsgHead msgHead) {
        JSONObject jsonObject = JSONObject.parseObject(command);
        // 组装传感器命令
        String chipCommand = chipMsgService.nettyDataToCommand(jsonObject);
        // 命令发送至mqtt
        boolean flag = mqttService.publish(MQTTTopicEnum.SENSOR_CONTROL, chipCommand);
        // 响应服务端处理结果
        NettyResponse nettyResponse = new NettyResponse(flag);
        NettyPacket<NettyResponse> nettyPacket = NettyPacket.buildResponse(msgHead, nettyResponse);
        nettyPacket.setMsgHead(msgHead);
        nettyClient.sendMsg(requestId, JSONObject.toJSONString(nettyPacket), false);
    }
}
