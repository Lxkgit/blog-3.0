package com.blog.pi.mqtt.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.ftp.FtpUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 单片机状态服务
 * @Author lxk
 * @CreateTime 2024-10-01
 */

@Service
public class ChipStatusService {

    private static final Logger logger = LoggerFactory.getLogger(ChipStatusService.class);

    private String MQTT_AUTHORIZATION  = "";

    @Value("${mqtt.ip}")
    private String ip;

    @Value("${mqtt.port}")
    private Integer port;

    /**
     * 登陆mqtt 获取token
     *
     * mqtt login接口返回数据
     * {
     *     "license": {
     *         "edition": "ce"
     *     },
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3Mjc3NzU1MTM0ODUsImlzcyI6IkVNUVgifQ.JYtcr0WwLu3nRV2mWsX2Hw7m4LC0Nvu2aEkU_ITaeiI",
     *     "version": "5.3.2"
     * }
     */
    public void loginMqtt() {

        try {
            Map<String, Object> param = new HashMap<>();
            param.put("username","admin");
            param.put("password","public");

            // 发送JSON格式请求
            HttpResponse res = HttpRequest.post("http://" + ip + ":18083/api/v5/login")
                    .header("Content-Type", "application/json")  // 关键：声明JSON格式
                    .body(JSONUtil.toJsonStr(param))            // 将Map转为JSON字符串
                    .execute();

            if ()
            JSONObject jsonObject = JSONObject.parseObject(result);
            MQTT_AUTHORIZATION = "Bearer " + jsonObject.get("token");

        } catch (Exception e) {
            logger.error("mqtt 登陆异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 获取mqtt服务中全部注册的clientId
     *
     * mqtt 返回数据格式
     * {
     *     "data": [],
     *     "meta": {
     *         "count": 0,
     *         "hasnext": false,
     *         "limit": 100,
     *         "page": 1
     *     }
     * }
     *  @param flag 登陆信息失效是否重新获取数据 true 重新登陆
     */
    public List<String> getMqttClientId(boolean flag) {

        try {
            List<String> clientId = new ArrayList<>();
            Map<String, Object> header = new HashMap<>();
            header.put("Authorization", MQTT_AUTHORIZATION);
            // 发送JSON格式请求
//            HttpResponse res = HttpRequest.post("http://" + ip + ":18083/api/v5/login")
//                    .header("Content-Type", "application/json")  // 关键：声明JSON格式
//                    .body(JSONUtil.toJsonStr(param))            // 将Map转为JSON字符串
//                    .execute();
            String result = HttpUtil.get("http://" + ip + ":18083/api/v5/clients");
            logger.info("result: {}", result);
            if (200 == 2100) {

//                JSONObject jsonObject = JSONObject.parseObject((String) result.get("data"));
//                JSONArray jsonArray = JSONArray.parseArray(jsonObject.getString("data"));
//                for (int i = 0; i < jsonArray.size(); i++) {
//                    JSONObject mqttClient = jsonArray.getJSONObject(i);
//                    clientId.add((String) mqttClient.get("clientid"));
//                }
//                jsonObject.getString("data");
//                return clientId;
            } else if (flag){
                logger.error("mqtt 登陆信息失效,重新获取mqtt登陆token信息");

                // 登陆mqtt
                loginMqtt();

                // 再次获取信息
                return getMqttClientId(false);
            } else {

            }
            return null;
        } catch (Exception e) {
            logger.error("mqtt 获取服务中全部注册的clientId异常：{}", e.getMessage(), e);
        }
        return null;
    }

}
