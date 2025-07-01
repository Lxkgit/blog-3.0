#include "stm32f10x.h"
#include "stdio.h"
#include "string.h"
#include "delay.h"
#include "usart.h"
#include "stm32f10x_it.h"
#include "esp8266.h"
#include "mqtt.h"





int main(void){
	delay_init();
	ESP8266_Init(115200);
	while(1)
	{
		ESP8266_STA_MQTTClient_Test();//≤‚ ‘MQTTÕ®—∂
		
	}
}
