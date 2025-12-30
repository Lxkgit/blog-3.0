//单片机头文件
#include "stm32f10x.h"

//网络设备
#include "esp8266.h"

//硬件驱动
#include "Timer.h"
#include "delay.h"
#include "usart.h"
#include "dht11.h"
#include "SGP30.h"


//C库
#include <string.h>
#include <stdio.h>

#define SENSOR_DATA		"stm32_001-%d|DHT11_01-%d_%d"
#define CHIP_SENSOR_REGISTER "stm32_001-气体检测模块|DHT11_01"


uint16_t Num = 0;			//定义在定时器中断里自增的变量
uint16_t secCount = 0;

u8 msgCount = 1;
u8 temp;
u8 humi;

uint16_t tvoc;
uint16_t co2;

void Hardware_Init(void)
{
	//中断控制器分组设置
	NVIC_PriorityGroupConfig(NVIC_PriorityGroup_2);

	//串口1，打印信息用
	Usart1_Init(115200);

	//串口2，驱动ESP8266用
	Usart2_Init(115200);

	//定时中断初始化
	Timer_Init();

	UsartPrintf(USART_DEBUG, "Hardware init OK\r\n");

}

void Chip_Register(void) {
	
	char atCommand[200];
	
	sprintf(atCommand, "AT+MQTTPUB=0,\"CHIP_SENSOR_REGISTER\",\"%s\",0,0\r\n", CHIP_SENSOR_REGISTER);
	
	ESP8266_SendCmd(atCommand, "OK");
}

void Send_Msg(void)
{
	char jsonStr[100];
	char atCommand[200];
	DHT11_Read_Data(&temp,&humi);
	UsartPrintf(USART_DEBUG, "P4--temp %d ,humi %d\r\n",temp,humi);
	
	// 1. 构建JSON字符串，内部双引号用\转义
	sprintf(jsonStr, SENSOR_DATA, msgCount, temp, humi);
	
	sprintf(atCommand, "AT+MQTTPUB=0,\"SENSOR_DATA\",\"%s\",0,0\r\n", jsonStr);
	
	ESP8266_SendCmd(atCommand, "OK");
	msgCount ++;
}

int main(void)
{

//	//初始化外围硬件
	Hardware_Init();

//	//初始化ESP8266
//	ESP8266_Init();

//	UsartPrintf(USART_DEBUG, "Connect MQTTs Server...\r\n");
//	while(ESP8266_SendCmd("AT+MQTTUSERCFG=0,1,\"STM32\",\"admin\",\"public\",0,0,\"\"\r\n", "OK"))
//		DelayMs(5000);
//	UsartPrintf(USART_DEBUG, "MQTT INIT ...\r\n");
//	while(ESP8266_SendCmd("AT+MQTTCONN=0,\"192.168.0.106\",1883,1\r\n", "CONNECTED"))
//		DelayMs(5000);
//	UsartPrintf(USART_DEBUG, "Connect MQTT Server Success\r\n");
	
//	Chip_Register();
	
	UsartPrintf(USART_DEBUG, "Init SGP30 ...\r\n");
	
	// 初始化 I2C 和 SGP30
	UsartPrintf(USART_DEBUG, "I2C Init OK ...\r\n");
 
	UsartPrintf(USART_DEBUG, "SGP30 Init OK ...\r\n");

	while(1)
	{
		UsartPrintf(USART_DEBUG, "Send SGP30 data...\r\n");
		
		DelayMs(5000);
		// 读取数据并返回

		UsartPrintf(USART_DEBUG, "Global TVOC: %d, CO2: %d\n", tvoc, co2);
	}
}

/**
  * 函    数：TIM2中断函数
  * 参    数：无
  * 返 回 值：无
  * 注意事项：此函数为中断函数，无需调用，中断触发后自动执行
  *           函数名为预留的指定名称，可以从启动文件复制
  *           请确保函数名正确，不能有任何差异，否则中断函数将不能进入
  */
void TIM2_IRQHandler(void)
{
	if (TIM_GetITStatus(TIM2, TIM_IT_Update) == SET)	//判断是否是TIM2的更新事件触发的中断
	{
		TIM_ClearITPendingBit(TIM2, TIM_IT_Update);			//清除TIM2更新事件的中断标志位
																										//中断标志位必须清除
																										//否则中断将连续不断地触发，导致主程序卡死
		secCount++;
		// 每秒中断一次，中断300次发送一次数据
		if(secCount >= 60) // 300秒 = 5分钟
    {
			Num ++;																				//Num变量自增，用于测试定时中断
			secCount = 0;
//      Send_Msg();
    }
	}
}


