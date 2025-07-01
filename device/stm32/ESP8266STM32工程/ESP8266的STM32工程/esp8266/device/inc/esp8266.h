#ifndef __ESP8266_H
#define __ESP8266_H 			   
#include "stm32f10x.h"

#include <stdio.h>
#include <string.h>
#include <stdbool.h>

#if defined ( __CC_ARM   )
#pragma anon_unions
#endif

extern char USART_ReceiveString[1024];

extern uint8_t Receive_sum = 0;

typedef enum{
    Multiple_ID_0 = 0,
    Multiple_ID_1 = 1,
    Multiple_ID_2 = 2,
    Multiple_ID_3 = 3,
    Multiple_ID_4 = 4,
    Single_ID_0 = 5,
} ENUM_ID_NO_TypeDef;

#define ESP8266_RST_Pin          GPIO_Pin_4    //复位管脚
#define ESP8266_RST_Pin_Port     GPIOA    //复位 
#define ESP8266_RST_Pin_Periph_Clock  RCC_APB2Periph_GPIOA       //复位时钟

#define ESP8266_CH_PD_Pin     GPIO_Pin_5   //使能管脚
#define ESP8266_CH_PD_Pin_Port     GPIOA   //使能端口
#define ESP8266_CH_PD_Pin_Periph_Clock  RCC_APB2Periph_GPIOA                     //使能时钟


#define ESP8266_RST_Pin_SetH     GPIO_SetBits(ESP8266_RST_Pin_Port,ESP8266_RST_Pin)
#define ESP8266_RST_Pin_SetL     GPIO_ResetBits(ESP8266_RST_Pin_Port,ESP8266_RST_Pin)


#define ESP8266_CH_PD_Pin_SetH     GPIO_SetBits(ESP8266_CH_PD_Pin_Port,ESP8266_CH_PD_Pin)
#define ESP8266_CH_PD_Pin_SetL     GPIO_ResetBits(ESP8266_CH_PD_Pin_Port,ESP8266_CH_PD_Pin)


#define ESP8266_USART(fmt, ...)  USART_printf (USART2, fmt, ##__VA_ARGS__)    
#define PC_USART(fmt, ...)       printf(fmt, ##__VA_ARGS__)       //这是串口打印函数，串口1，执行printf后会自动执行fput函数，重定向了printf。



#define RX_BUF_MAX_LEN 1024       //最大字节数
//数据帧结构体
//char Data_RX_BUF[RX_BUF_MAX_LEN];
//u16 FramLength;
//u16 FramFinishFlag;



//初始化和TCP功能函数
void ESP8266_Init(u32 bound);
void ESP8266_AT_Test(void);
bool ESP8266_Send_AT_Cmd(char *cmd,char *ack1,char *ack2,u32 time);
void ESP8266_Rst(void);
bool ESP8266_Net_Mode_Choose(void);
bool ESP8266_JoinAP( char * pSSID, char * pPassWord );
bool ESP8266_SendString(FunctionalState enumEnUnvarnishTx, char * pStr, u32 ulStrLength, ENUM_ID_NO_TypeDef ucId );
void USART_printf( USART_TypeDef * USARTx, char * Data, ... );

//MQTT功能函数
bool ESP8266_MQTTUSERCFG( char * pClient_Id, char * pUserName,char * PassWord);
bool ESP8266_MQTTCONN( char * Ip, int Num);
bool ESP8266_MQTTSUB(char * Topic);
bool ESP8266_MQTTPUB( char * Topic,char *temp);
bool ESP8266_MQTTCLEAN(void);
bool MQTT_SendString(char * pTopic,char *temp2);

#endif
