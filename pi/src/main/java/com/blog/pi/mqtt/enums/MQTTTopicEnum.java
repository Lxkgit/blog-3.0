package com.blog.pi.mqtt.enums;

public enum MQTTTopicEnum {

    // 芯片传感器设备注册
    CHIP_SENSOR_REGISTER("CHIP_SENSOR_REGISTER", 1),
    /*
    {
    	"chipName": "电脑开关机",
    	"chipCode": "stm32",
    	"chipType": "stm32",
    	"memo": "备注信息",
    	"sensorList": [
    		{
    			"sensorName": "温湿度",
    			"sensorCode": "aqwe12",
    			"sensorType": "DHT11",
    			"memo": "温湿度传感器备注",
    		},
    		{
    			"sensorName": "180度舵机",
    			"sensorCode": "duo-180",
    			"sensorType": "DUO-180",
    			"memo": "舵机备注",
    		},
    		{
    			"sensorName": "360度舵机",
    			"sensorCode": "duo-360",
    			"sensorType": "DUO-360",
    			"memo": "舵机备注",
    		},
    	]
    }
     */
    // 传感器数据
    SENSOR_DATA("SENSOR_DATA", 0),
    /*
    {
    	"chipCode": "stm32",
    	"msgCode": "asd75z",
    	"msgCount": "1",
    	"dataList": [
    		{
    			"sensorCode": "aqwe12",
    			"sensorData": "[{\"value\":\"20.60\",\"key\":\"温度\"},{\"value\":\"40.00\",\"key\":\"湿度\"}]"
    		},
    		{
    			"sensorCode": "asq124",
    			"sensorData": "[{\"value\":\"20.60\",\"key\":\"二氧化碳浓度\"}]"
    		},
    		{
    			"sensorCode": "dsg157",
    			"sensorData": "[{\"value\":\"20\",\"key\":\"声音大小(dB)\"}]"
    		}
    	]
    }
     */

    // 传感器控制
    SENSOR_CONTROL("SENSOR_CONTROL", 0),
    /*
    {
    	"chipCode": "stm32",
    	"commandList": [
    		{
    		    "delay": 1000,
    			"sensorCode": "asq124",
    			"idx": 0,
    			"data": 180
    		},
    			{
    		    "delay": 1000,
    			"sensorCode": "asq124",
    			"idx": 0,
    			"data": 1
    		},
    			{
    		    "delay": 1000,
    			"sensorCode": "asq124",
    			"idx": 0,
    			"data": 0
    		},
    	]
    }
     */
    ;


    /**
     * topic
     */
    private String topic;

    /**
     * mqtt qos 等级
     */
    private Integer qos;

    MQTTTopicEnum(String topic, Integer qos) {
        this.topic = topic;
        this.qos = qos;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getQos() {
        return qos;
    }

    public void setQos(Integer qos) {
        this.qos = qos;
    }
}
