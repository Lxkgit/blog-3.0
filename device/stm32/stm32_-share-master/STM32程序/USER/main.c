#include "led.h"
#include "delay.h"
#include "sys.h"
#include "usart.h"
#include "usart2.h"
#include "dht11.h"
#include "wifi.h"
#include "timer.h"
#include <string.h>
char buf1[100];

char *cmdLEDON = "+MQTTSUBRECV:0,\"subtest\",5,LEDON";
char *cmdLEDOFF = "+MQTTSUBRECV:0,\"subtest\",6,LEDOFF";

int main(void)
{
	delay_init();	    	 //延时函数初始化	  
	LED_Init();		  	//初始化与LED连接的硬件接口
	wifi_GPIO_Init();
	uart_init(9600);
	USART2_Init(115200); //连接ESP8266
	DHT11_Init();  //dht11
	rst_wifi();
	init_wifi();
	TIM3_Int_Init(9999,35999); //5s
	while(1)
	{
		if(USART2_RX_STA&0X8000)
		{
			strcpy(buf1,(char*)USART2_RX_BUF);
			//printf("串口二收到:%s\r\n",buf1);
			if(!memcmp(buf1,cmdLEDON,strlen(cmdLEDON)))
			{
				//开灯
				LED1 = 0;
				printf("开灯\r\n");
			}
			if(!memcmp(buf1,cmdLEDOFF,strlen(cmdLEDOFF)))
			{
				//关灯
				LED1 = 1;
				printf("关灯\r\n");
			}
			
			USART2_RX_STA = 0;
		}
	}
}

