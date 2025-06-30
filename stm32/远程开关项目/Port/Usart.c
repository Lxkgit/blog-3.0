#include "usart.h"
#include "esp8266.h"


#pragma import(__use_no_semihosting)             

struct __FILE 
{
	int handle;
};

FILE __stdout;       

void _sys_exit(int x) 
{ 
	x = x; 
} 

int fputc(int ch, FILE *f)
{

  while (USART_GetFlagStatus(USART3, USART_FLAG_TXE) == RESET)
  {}
	USART_SendData(USART3, (uint8_t) ch);


  while (USART_GetFlagStatus(USART3, USART_FLAG_TC) == RESET){}

	return ch;
}

/**
 * 串口2初始化 
 * 用于连接esp8266首发信息
 * 引脚为 PA2 PA3
 */
void usart2_Init() {
	
	RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART2, ENABLE);
	// 使能指定端口时钟
	RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);
	
	// GPIO初始化
	GPIO_InitTypeDef  GPIO_InitStructure;
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2;
	GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
	// 复用推挽输出
	GPIO_InitStructure.GPIO_Mode=GPIO_Mode_AF_PP;
	// 初始化GPIO
	GPIO_Init(GPIOA, &GPIO_InitStructure);
	
	GPIO_InitStructure.GPIO_Pin = GPIO_Pin_3;
	GPIO_InitStructure.GPIO_Mode=GPIO_Mode_IN_FLOATING;
	GPIO_Init(GPIOA, &GPIO_InitStructure);
	
	// Usart2 NVIC 配置
	NVIC_InitTypeDef NVIC_InitStructure;
	NVIC_InitStructure.NVIC_IRQChannel = USART2_IRQn;
	NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority=0;
	NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;      
	NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;         
	NVIC_Init(&NVIC_InitStructure);
	
	// USART2 配置
	USART_InitTypeDef USART_InitStructure;
	USART_InitStructure.USART_BaudRate = 115200; 	// 波特率
	USART_InitStructure.USART_WordLength = USART_WordLength_8b; // 字长为8
	USART_InitStructure.USART_StopBits = USART_StopBits_1; // 1个停止位
	USART_InitStructure.USART_Parity = USART_Parity_No; // 无奇偶校验
	USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_None; // 无流控
	USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx; // 收发模式
	USART_Init(USART2, &USART_InitStructure); // 配置 USART2 参数
	
	// 配置了接受中断和总线空闲中断
	USART_ITConfig(USART2, USART_IT_RXNE|USART_IT_IDLE, ENABLE);
	
	// 使能 USART2
	USART_Cmd(USART2, ENABLE);
}

void USART2_IRQHandler(void) {
    u8 ucCh;

    if(USART_GetITStatus(USART2, USART_IT_RXNE) != RESET){
        ucCh = USART_ReceiveData(USART2);
        if(ESP8266_Fram_Record_Struct.InfBit.FramLength < (RX_BUF_MAX_LEN-1)) {
            ESP8266_Fram_Record_Struct.Data_RX_BUF[ESP8266_Fram_Record_Struct.InfBit.FramLength++] = ucCh;   
        }                    
    }
}


