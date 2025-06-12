package com.blog.pi.mqtt;

import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.mqtt.enums.MQTTTopicEnum;
import com.blog.pi.mqtt.http.ChipStatusService;
import com.blog.pi.netty.client.NettyClient;
import com.blog.pi.netty.dto.NettyPacket;
import com.blog.pi.netty.enums.NettyPacketType;
import com.blog.pi.netty.enums.NettyTopicEnum;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttMessageListener implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(MqttMessageListener.class);

    private final NettyClient nettyClient = SpringUtils.getBean(NettyClient.class);

    private final MqttService mqttService = SpringUtils.getBean(MqttService.class);

    private final ChipStatusService chipStatusService = SpringUtils.getBean(ChipStatusService.class);

    /**
     * mqtt断线重连
     *
     * @param throwable
     */
    @Override
    public void connectionLost(Throwable throwable) {
        int num = 1;
        while (true) {
            try {
                logger.info("mqtt 重新连接 num:{}", num);
                mqttService.getMqttClient().reconnect();
                if (mqttService.getMqttClient().isConnected()) {
                    // 判断已经重新连接成功  需要重新订阅主题 可以在这个if里面订阅主题  或者 connectComplete（方法里面）
                    logger.warn("MQTT 重新连接成功");
                    mqttService.subscribe();
                    return;
                }
                num++;
            } catch (MqttException e) {
                logger.error("mqtt断连异常", e);
            }
            try {
                // 5秒执行异常重新连接
                Thread.sleep(5000);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
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
            logger.info("MQTT Topic:{} data:{}", topic, data);
            chipStatusService.loginMqtt();
            if (topic.equals(MQTTTopicEnum.CHIP_SENSOR_REGISTER.getTopic())) {

                // 发送 Netty 单片机设备注册消息
                NettyPacket<String> nettyRequest = NettyPacket.buildRequest(data);
                nettyRequest.setNettyPacketType(NettyPacketType.REQUEST.getValue());
                nettyRequest.setTopic(NettyTopicEnum.CHIP_SENSOR_REGISTER.getTopic());
                nettyClient.sendMsg(nettyRequest.getRequestId(), JSONObject.toJSONString(nettyRequest), true);
            } else if (topic.equals(MQTTTopicEnum.SENSOR_DATA.getTopic())) {

                // 发送 Netty 传感器数据
                NettyPacket<String> nettyRequest = NettyPacket.buildRequest(data);
                nettyRequest.setNettyPacketType(NettyPacketType.REQUEST.getValue());
                nettyRequest.setTopic(NettyTopicEnum.SENSOR_DATA.getTopic());
                nettyClient.sendMsg(nettyRequest.getRequestId(), JSONObject.toJSONString(nettyRequest), true);
            }

//            MQTTSensorData mqttSensorData = JSONObject.toJavaObject(JSONObject.parseObject(data), MQTTSensorData.class);
//            NettyPacket<MQTTSensorData> nettyRequest = NettyPacket.buildRequest(mqttSensorData);
//            nettyRequest.setNettyPacketType(NettyPacketType.REQUEST.getValue());
//            nettyRequest.setTopic(NettyTopicEnum.BLOG_SENSOR_DATA.getTopic());
//            nettyClient.sendMsg(JSONObject.toJSONString(nettyRequest));
        } catch (Exception e) {
            logger.error("mqtt 消息处理异常：{}", e.getMessage(), e);
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
            logger.info("mqtt 发送消息：{}", token.getMessage());
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}
