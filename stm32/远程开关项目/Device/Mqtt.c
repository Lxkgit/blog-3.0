#include "mqtt.h"
#include "esp8266.h"
#include "constants.h"

/**
 * 服务启动时初始化mqtt
 */
void MQTT_Init() {
	// esp8266 初始化
	ESP8266_Init();
	ESP8266_AT_Test();
	// 设置esp8266工作模式
	ESP8266_Mode();
	// 连接WiFi
	ESP8266_WiFi_Connect(WIFI_USERNAME, WIFI_PASSWORD);
	// 连接mqtt
	MQTT_Connect();
	// mqtt订阅topic
	//MQTT_Sub_Topic();
}

/**
 * MQTT 连接服务器
 */
void MQTT_Connect() {
	// 配置mqtt登陆信息
	ESP8266_MQTTUSERCFG(MQTT_CLIENT_ID, MQTT_USERNAME, MQTT_PASSWORD);
	ESP8266_MQTT_Connect(MQTT_IP, MQTT_PORT);
}

/**
 * MQTT 订阅topic
 */
void MQTT_Sub_Topic() {
	ESP8266_MQTT_Sub("");
}

/**
 * mqtt发送数据至服务器
 * topic：mqtt中topic
 * str：mqtt发送数 需要json格式
 */
void MQTT_Send_String(char *topic, char *str) {
	
	if(ESP8266_MQTT_Pub(topic, str)) {
		
	} else {
		MQTT_Re_Send_String(topic, str);
	}
    
}

/**
 * mqtt数据发送失败时重发消息
 * topic：mqtt中topic
 * str：mqtt发送数 需要json格式
 */
void MQTT_Re_Send_String(char *topic, char *str) {
	
	// 发送失败 测试mqtt是否连接正常
	if(ESP8266_MQTT_Connect_Test()) {
		// mqtt连接正常
		
	} else {
		// mqtt连接异常重新连接
		ESP8266_MQTT_Connect(MQTT_IP, MQTT_PORT);
		ESP8266_MQTT_Sub("");
	}
	ESP8266_MQTT_Pub(topic, str);
}
