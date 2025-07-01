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
	ESP8266_MQTTSUB("topic_sub");
	printf("\r\nMQTT配置完成");
	while(1) {
		delay_ms(300);
		if (strstr(USART_ReceiveString, "OK")) {
				
			u16 l = Receive_sum;
			sprintf(str, "receive - length - %d", l);
			MQTT_SendString("topic_receive", str);
			
//			char ch = ESP8266_Fram_Record_Struct.Data_RX_BUF[0];
//			char ch1 = ESP8266_Fram_Record_Struct.Data_RX_BUF[1];
//			char ch2 = ESP8266_Fram_Record_Struct.Data_RX_BUF[2];
//			sprintf(str, "receive -- %ch - %ch - %ch", ch, ch1, ch2);
//			MQTT_SendString("topic_receive", str);
					
//			char ch[300];
//			for(int i=0; ESP8266_Fram_Record_Struct.Data_RX_BUF[i] != '\0'; i++) {
//				ch[i] = ESP8266_Fram_Record_Struct.Data_RX_BUF[i];
//			}
				
			char ch[300];
			for(int i=0; i<10; i++) {
				ch[i] = USART_ReceiveString[i];
			}
			sprintf(str, "receive -- %s", ch);
			MQTT_SendString("topic_receive", str);
		}	
	}
}
