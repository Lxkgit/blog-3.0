package com.blog.pi.mqtt;

import com.blog.pi.mqtt.enums.MQTTTopicEnum;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @Description mqtt服务客户端
 * @Author lxk
 * @CreateTime 2025-06-12
 */

@Component
public class MqttService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);

    @Value("${mqtt.ip}")
    private String ip;

    @Value("${mqtt.port}")
    private Integer port;

    @Value("${mqtt.username}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.clientId}")
    private String clientId;

    private MqttClient client;

    @Override
    public void run(String... args) throws Exception {
        try {
            String url = "tcp://" + ip + ":" + port;
            client = new MqttClient(url, clientId, new MemoryPersistence());

            MqttConnectOptions options = getMqttConnectOptions();

            // 消息回调函数
            client.setCallback(new MqttMessageListener());
            client.setTimeToWait(5000);
            client.connect(options);
            logger.info("mqtt连接成功，ip:{} port:{}", ip, port);

            // 订阅 topic
            subscribe();

        } catch (Exception e) {
            logger.error("mqtt 连接报错:{}", e.getMessage(), e);
        }
    }

    /**
     * 配置mqtt连接参数
     *
     * @return
     */
    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，
        // 这里设置为true表示每次连接到服务器都以新的身份连接
        options.setCleanSession(true);
        // 设置连接的用户名
        options.setUserName(username);
        // 设置连接的密码
        options.setPassword(password.toCharArray());
        // 设置超时时间 单位为秒
        options.setConnectionTimeout(100);
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送心跳判断客户端是否在线，但这个方法并没有重连的机制
        options.setKeepAliveInterval(20);
        return options;
    }


    /**
     * 订阅某个主题，qos默认为0
     */
    public void subscribe() {
        subscribe(MQTTTopicEnum.CHIP_SENSOR_REGISTER.getTopic(), MQTTTopicEnum.CHIP_SENSOR_REGISTER.getQos());
        subscribe(MQTTTopicEnum.SENSOR_DATA.getTopic(), MQTTTopicEnum.SENSOR_DATA.getQos());
    }

    /**
     * 订阅某个主题
     *
     * @param topic
     * @param qos
     */
    public void subscribe(String topic, int qos) {
        try {
            client.subscribe(topic, qos);
        } catch (MqttException e) {
            logger.error("mqtt 订阅主题异常topic:{} error:{}", topic, e.getMessage(), e);
        }
    }

    public MqttClient getMqttClient() {
        return client;
    }

    /**
     * 发布，默认qos为0，非持久化
     *
     * @param topic
     * @param pushMessage
     */
    public void publish(String topic, String pushMessage) {
        publish(1, false, topic, pushMessage);
    }

    /**
     * 发布
     *
     * @param qos
     * @param retained
     * @param topic
     * @param pushMessage
     */
    public synchronized void publish(int qos, boolean retained, String topic, String pushMessage) {
        if (client.isConnected()) {
            MqttMessage message = new MqttMessage();
            message.setQos(qos);
            message.setRetained(retained);
            message.setPayload(pushMessage.getBytes());
            MqttTopic mTopic = client.getTopic(topic);
            if (null == mTopic) {
                logger.error("topic not exist");
                return;
            }
            MqttDeliveryToken token;
            try {
                token = mTopic.publish(message);
                token.waitForCompletion();
            } catch (Exception e) {
                logger.info(e.getMessage(), e);
            }
        } else {
            logger.error("Mqtt not connected");
        }
    }
}
