package com.blog.pi.mqtt.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.blog.pi.mqtt.enums.seneor.SensorTypeEnum;
import com.blog.pi.utils.MyStringUtils;
import org.springframework.stereotype.Service;

/**
 * @Description 单片机消息转换服务
 * @Author lxk
 * @CreateTime 2025-11-26
 */

@Service
public class ChipMsgService {

    /**
     * 设备上报消息转换为json格式
     * 数据消息: {单片机型号_单片机序号}-{消息发送序号}|{传感器型号_传感器序号}-{数据1_数据2}|{传感器型号_传感器序号}-{数据1_数据2}|...
     * 树莓派上报数据:
     * {
     *     	"chipCode": "stm32_001",
     *     	"msgCode": "asd75z",
     *     	"msgCount": "1",
     *     	"dataList": [
     *     		  {
     *     			"sensorCode": "DHT11_001",
     *     			"sensorData": "{\"temp\":\"20.60\",\"humi\":\"40.00\"}"
     *            },
     *            {
     *     			"sensorCode": "SCD41_001",
     *     			"sensorData": "{\"co2\":\"20.60\"}"
     *            },
     *            {
     *     			"sensorCode": "MAX9814_001",
     *     			"sensorData": "{\"sound\":\"20.5\"}"
     *            }
     *     	]
     * }
     * @param data
     * @return
     */
    public JSONObject chipDataToJson(String data) {
        String[] sensorDataArr = data.split("\\|");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("chipCode", sensorDataArr[0].split("-")[0]);
        jsonObject.put("msgCode", MyStringUtils.getRandomString(6));
        jsonObject.put("msgCount", sensorDataArr[0].split("-")[1]);
        JSONArray dataList = new JSONArray();
        for (int i = 1; i < sensorDataArr.length; i++) {
            JSONObject dataObj = new JSONObject();
            String sensorCode = sensorDataArr[i].split("-")[0];
            dataObj.put("sensorCode", sensorCode);

            String sensorData = sensorDataArr[i].split("-")[1];
            dataObj.put("sensorData", SensorTypeEnum.getSensorData(sensorCode, sensorData));
            dataList.add(dataObj);
        }
        jsonObject.put("dataList", dataList);
        return jsonObject;
    }

    /**
     * 设备注册消息转换为json格式
     * 注册消息: {单片机型号_单片机序号}-{单片机名称}|{传感器型号_传感器序号}|{传感器型号_传感器序号}|...
     * {
     *     	"chipName": "电脑开关机",
     *     	"chipCode": "stm32_001",
     *     	"chipType": "stm32",
     *     	"sensorList": [
     *     		  {
     *     			"sensorCode": "DHT11_001",
     *     			"sensorType": "DHT11"
     *            },
     *            {
     *     			"sensorCode": "DUO180_001",
     *     			"sensorType": "DUO180"
     *            },
     *            {
     *     			"sensorCode": "DUO360_001",
     *     			"sensorType": "DUO360"
     *            }
     *     	]
     * }
     * @param data
     * @return
     */
    public JSONObject chipRegisterToJson(String data) {
        String[] chipRegisterArr = data.split("\\|");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("chipCode", chipRegisterArr[0].split("-")[0]);
        jsonObject.put("chipType", chipRegisterArr[0].split("-")[0].split("_")[0]);
        jsonObject.put("chipName", chipRegisterArr[0].split("-")[1]);
        JSONArray sensorList = new JSONArray();
        for (int i = 1; i < chipRegisterArr.length; i++) {
            JSONObject dataObj = new JSONObject();
            String sensorType = chipRegisterArr[i].split("_")[0];
            dataObj.put("sensorType", sensorType);
            String sensorCode = chipRegisterArr[i];
            dataObj.put("sensorCode", sensorCode);
            sensorList.add(dataObj);
        }
        jsonObject.put("sensorList", sensorList);
        return jsonObject;
    }

    /**
     * 接收netty消息转换为设备控制命令
     * 控制命令: {单片机型号_单片机序号}|{传感器型号_传感器序号}-{参数1_参数2}|{传感器型号_传感器序号}-{参数1_参数2}|...
     * {
     *     	"chipCode": "stm32_001",
     *     	"commandList": [
     *     		  {
     *     		    "delay": 1000,
     *     			"sensorCode": "DUO180_001",
     *     			"idx": 1,
     *     			"data": 180
     *            },
     *            {
     *     		    "delay": 1000,
     *     			"sensorCode": "DUO360_001",
     *     			"idx": 2,
     *     			"data": 220
     *            },
     *            {
     *     		    "delay": 1000,
     *     			"sensorCode": "DUO180_001",
     *     			"idx": 3,
     *     			"data": 90
     *            }
     *     	]
     * }
     * @param command
     * @return
     */
    public String nettyDataToCommand(JSONObject command) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(command.get("chipCode"));
        JSONArray commandArr = (JSONArray) command.get("commandList");
        for (Object o : commandArr) {
            JSONObject commandObj = (JSONObject) o;
            buffer.append("|");
            buffer.append(commandObj.get("sensorCode")).append("-");
            buffer.append(commandObj.get("delay")).append("_").append(commandObj.get("data"));
        }
        return buffer.toString();
    }
}
