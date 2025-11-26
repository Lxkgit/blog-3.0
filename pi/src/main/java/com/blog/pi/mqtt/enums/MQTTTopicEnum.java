package com.blog.pi.mqtt.enums;

public enum MQTTTopicEnum {

    // 芯片传感器设备注册
    CHIP_SENSOR_REGISTER("CHIP_SENSOR_REGISTER", 1),
    /*
    {
    	"chipName": "电脑开关机",
    	"chipCode": "stm32_001",
    	"chipType": "stm32",
    	"sensorList": [
    		{
    			"sensorCode": "DHT11_001",
    			"sensorType": "DHT11"
    		},
    		{
    			"sensorCode": "DUO180_001",
    			"sensorType": "DUO180"
    		},
    		{
    			"sensorCode": "DUO360_001",
    			"sensorType": "DUO360"
    		}
    	]
    }
     */
    // 传感器数据
    SENSOR_DATA("SENSOR_DATA", 0),
    /*
    {
    	"chipCode": "stm32_001",
    	"msgCode": "asd75z",
    	"msgCount": "1",
    	"dataList": [
    		{
    			"sensorCode": "DHT11_001",
    			"sensorData": "{\"temp\":\"20.60\",\"humi\":\"40.00\"}"
    		},
    		{
    			"sensorCode": "SCD41_001",
    			"sensorData": "{\"co2\":\"20.60\"}"
    		},
    		{
    			"sensorCode": "MAX9814_001",
    			"sensorData": "{\"sound\":\"20.5\"}"
    		}
    	]
    }
     */

    // 传感器控制
    SENSOR_CONTROL("SENSOR_CONTROL", 0),
    /*
    {
    	"chipCode": "stm32_001",
    	"commandList": [
    		{
    		    "delay": 1000,
    			"sensorCode": "DUO180_001",
    			"idx": 1,
    			"data": 180
    		},
    			{
    		    "delay": 1000,
    			"sensorCode": "DUO360_001",
    			"idx": 2,
    			"data": 220
    		},
    			{
    		    "delay": 1000,
    			"sensorCode": "DUO180_001",
    			"idx": 3,
    			"data": 90
    		}
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
