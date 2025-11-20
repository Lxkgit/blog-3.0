//单片机头文件
#include "stm32f10x.h"

//网络设备
#include "esp8266.h"

//硬件驱动
#include "delay.h"
#include "usart.h"
#include "led.h"
#include "key.h"
#include "dht11.h"

//C库
#include <string.h>
#include <stdio.h>


void Hardware_Init(void)
{
	//中断控制器分组设置
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);	

	//systick初始化
	Delay_Init();									

	//串口1，打印信息用
	Usart1_Init(115200);							

	//串口2，驱动ESP8266用
	Usart2_Init(115200);							

	Key_Init();

	//蜂鸣器初始化
	Led_Init();

	UsartPrintf(USART_DEBUG, " Hardware init OK\r\n");

}


u8 temp;
u8 humi;
int main(void)
{
	int i;
	char jsonStr[100];
	char atCommand[200];

	//初始化外围硬件
	Hardware_Init();

	//初始化ESP8266
	ESP8266_Init();

	UsartPrintf(USART_DEBUG, "Connect MQTTs Server...\r\n");
	while(ESP8266_SendCmd("AT+MQTTUSERCFG=0,1,\"STM32\",\"admin\",\"public\",0,0,\"\"\r\n", "OK"))
		DelayXms(5000);
	UsartPrintf(USART_DEBUG, "MQTT INIT ...\r\n");
	while(ESP8266_SendCmd("AT+MQTTCONN=0,\"192.168.0.106\",1883,1\r\n", "CONNECTED"))
		DelayXms(5000);
	UsartPrintf(USART_DEBUG, "Connect MQTT Server Success\r\n");
	
	while(1)
	{
		DHT11_Read_Data(&temp,&humi);
		UsartPrintf(USART_DEBUG, "P4--temp %d ,humi %d\r\n",temp,humi);
	
		// 1. 构建JSON字符串，内部双引号用\转义
		sprintf(jsonStr, "{\\\"temp\\\":%d\\\, \\\"humi\\\":%d}", temp, humi);

		// 把所有单引号替换为双引号
		
		for (i = 0; jsonStr[i] != '\0'; i++) {
			if (jsonStr[i] == '\'') {
				jsonStr[i] = '"';
			}
		}
		
		sprintf(atCommand, "AT+MQTTPUB=0,\"pubtest\",\"%s\",0,0\r\n", jsonStr);
		
		ESP8266_SendCmd(atCommand, "OK");
		
		DelayMs(10000);
	}
}
