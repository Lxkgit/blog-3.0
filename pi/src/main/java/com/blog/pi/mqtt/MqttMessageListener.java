package com.blog.pi.mqtt;

import com.alibaba.fastjson2.JSONObject;
import com.blog.core.domain.netty.dto.NettyPacket;
import com.blog.core.domain.netty.enums.NettyTopicEnum;
import com.blog.pi.mqtt.enums.MQTTTopicEnum;
import com.blog.pi.mqtt.http.ChipStatusService;
import com.blog.pi.mqtt.service.ChipMsgService;
import com.blog.pi.netty.client.NettyClient;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class MqttMessageListener implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttMessageListener.class);

    private final NettyClient nettyClient = SpringUtils.getBean(NettyClient.class);

    private final MqttService mqttService = SpringUtils.getBean(MqttService.class);

    private final ChipStatusService chipStatusService = SpringUtils.getBean(ChipStatusService.class);

    private final ChipMsgService chipMsgService = SpringUtils.getBean(ChipMsgService.class);

    private final AtomicBoolean reconnecting = new AtomicBoolean(false);

    /**
     * mqtt断线重连
     *
     * @param throwable
     */
    @Override
    public void connectionLost(Throwable throwable) {
        logger.warn("MQTT 连接丢失", throwable);

        if (!reconnecting.compareAndSet(false, true)) {
            return; // 已有重连线程在跑
        }

        new Thread(() -> {
            try {
                int num = 1;
                while (!mqttService.getMqttClient().isConnected()) {
                    try {
                        logger.info("MQTT 重新连接 连接次数:{}", num++);
                        mqttService.getMqttClient().reconnect();
                    } catch (Exception e) {
                        logger.warn("MQTT 重连失败，5秒后重试");
                        Thread.sleep(5000);
                    }
                }
                logger.info("MQTT 重新连接成功");
                mqttService.subscribe();
            } catch (Exception e) {
                logger.error("MQTT 重连线程异常", e);
            } finally {
                reconnecting.set(false);
            }
        }, "mqtt-reconnect-thread").start();
    }

    /**
     * mqtt消息接收
     *
     * @param topic
     * @param message
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String data = new String(message.getPayload());
            logger.info("===== MQTT 收到消息 ===== Topic:{} data:{}", topic, data);
            chipStatusService.getMqttClientId(true);
            if (topic.equals(MQTTTopicEnum.CHIP_SENSOR_REGISTER.getTopic())) {
                // 消息转换为json格式
                JSONObject chipRegisterJson = chipMsgService.chipRegisterToJson(data);
                // 发送 Netty 单片机设备注册消息
                NettyPacket<JSONObject> nettyRequest = NettyPacket.buildRequest(NettyTopicEnum.CHIP_SENSOR_REGISTER.getTopic(), chipRegisterJson);
                nettyClient.sendMsg(nettyRequest.getMsgHead().getNettyMsgHead().getRequestId(), JSONObject.toJSONString(nettyRequest), true);
            } else if (topic.equals(MQTTTopicEnum.SENSOR_DATA.getTopic())) {
                // 消息转换为json格式
                JSONObject chipJson = chipMsgService.chipDataToJson(data);
                // 发送 Netty 传感器数据
                NettyPacket<JSONObject> nettyRequest = NettyPacket.buildRequest(NettyTopicEnum.SENSOR_DATA.getTopic(), chipJson);
                nettyClient.sendMsg(nettyRequest.getMsgHead().getNettyMsgHead().getRequestId(), JSONObject.toJSONString(nettyRequest), true);
            }
        } catch (Exception e) {
            logger.error("MQTT 消息处理异常：{}", e.getMessage(), e);
        }
    }

    /**
     * mqtt消息发送完成 回调消息
     *
     * @param token
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            logger.info("MQTT 发送消息：{}", token.getMessage());
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
