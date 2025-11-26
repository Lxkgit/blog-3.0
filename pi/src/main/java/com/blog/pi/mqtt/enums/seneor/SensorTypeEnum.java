package com.blog.pi.mqtt.enums.seneor;

import java.text.MessageFormat;

/**
 * @author lxk
 * @description 传感器类型枚举类
 * @date 2025/11/26
 */

public enum SensorTypeEnum {

    DTH11("DHT11", "{\"temp\":\"{0}\",\"humi\":\"{1}\"}", null),
    DUO180("DUO180", null, ""),
    ;

    private String sensorType;

    private String dataTemplate;

    private String commandTemplate;

    public static String getSensorData(String sensorCode, String sensorData) {
        for (SensorTypeEnum sensorTypeEnum : SensorTypeEnum.values()) {
            if (sensorCode.startsWith(sensorTypeEnum.getSensorType())) {
                return MessageFormat.format(sensorTypeEnum.getDataTemplate(), (Object[]) sensorData.split("_"));
            }
        }
        return null;
    }

    public static String setSensorCommand(String sensorCode, String sensorCommand) {
        for (SensorTypeEnum sensorTypeEnum : SensorTypeEnum.values()) {
            if (sensorCode.startsWith(sensorTypeEnum.getSensorType())) {

            }
        }
        return null;
    }

    SensorTypeEnum(String sensorType, String dataTemplate, String commandTemplate) {
        this.sensorType = sensorType;
        this.dataTemplate = dataTemplate;
        this.commandTemplate = commandTemplate;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getDataTemplate() {
        return dataTemplate;
    }

    public void setDataTemplate(String dataTemplate) {
        this.dataTemplate = dataTemplate;
    }

    public String getCommandTemplate() {
        return commandTemplate;
    }

    public void setCommandTemplate(String commandTemplate) {
        this.commandTemplate = commandTemplate;
    }
}
