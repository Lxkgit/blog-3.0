#ifndef __ESP8266_H
#define __ESP8266_H 			   
#include "stm32f10x.h"

#include <stdio.h>
#include <string.h>
#include <stdbool.h>

#if defined ( __CC_ARM   )
#pragma anon_unions
#endif

#define RX_BUF_MAX_LEN 1024       // mqtt接收最大字节数
extern struct STRUCT_USART_Fram   // 数据帧结构体
{
    char Data_RX_BUF[RX_BUF_MAX_LEN];
    union {
        __IO u16 InfAll;
        struct {
            __IO u16 FramLength       :15;                               // 14:0 
            __IO u16 FramFinishFlag   :1;                                // 15 
        }InfBit;
    }; 
	
}ESP8266_Fram_Record_Struct;

#define ESP8266_USART(fmt, ...)  USART_printf (USART2, fmt, ##__VA_ARGS__) 

void ESP8266_Init(void);
void ESP8266_AT_Test(void);
bool ESP8266_Mode(void);
bool ESP8266_MQTTUSERCFG(char *pClient_Id, char *pUserName,char *PassWord);
bool ESP8266_WiFi_Connect(char *pSSID, char *pPassWord);
bool ESP8266_MQTT_Connect(char *ip, int port);

bool ESP8266_MQTT_Connect_Test(void);
bool ESP8266_MQTT_Sub(char *topic);
bool ESP8266_MQTT_Pub(char *topic, char *str);

#endif
