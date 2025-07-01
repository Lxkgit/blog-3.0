#include "mqtt.h"
#include "esp8266.h"
#include "delay.h"
#include "usart.h"

void ESP8266_STA_MQTTClient_Test(void)
{
	char str[1024];
	printf("正在配置ESP8266参数\r\n");
    ESP8266_AT_Test();//恢复出厂默认模式
    ESP8266_Net_Mode_Choose();
    while(!ESP8266_JoinAP(User_ESP8266_SSID, User_ESP8266_PWD));
	  ESP8266_MQTTUSERCFG(User_ESP8266_client_id,User_ESP8266_username,User_ESP8266_password);
	  ESP8266_MQTTCONN( User_ESP8266_MQTTServer_IP, User_ESP8266_MQTTServer_PORT);
	  ESP8266_MQTTSUB( User_ESP8266_MQTTServer_Topic);
		printf("\r\nMQTT配置完成");
		while(1) {
			sprintf(str,"aithinker");//格式化发送字符串到MQTT服务器
			MQTT_SendString("topic_send",str);//发送数据到MQTT服务器
			delay_ms(1000);
				
			sprintf(str,"topic_receive--1");
			MQTT_SendString("topic_receive", str);
			
			if (strstr(ESP8266_Fram_Record_Struct.Data_RX_BUF, "OK")) {
				char* s1 = ESP8266_Fram_Record_Struct.Data_RX_BUF;

				int x = 88;
				sprintf(str, "receive - %d - %s", x, "111");
				MQTT_SendString("topic_receive", str);
				u16 l = ESP8266_Fram_Record_Struct.InfBit.FramLength;
					
				sprintf(str, "receive -- %d", l);
				MQTT_SendString("topic_receive", str);
				
				sprintf(str, "receive -- %s", s1);
				MQTT_SendString("topic_receive", str);
			}	
		}
}
