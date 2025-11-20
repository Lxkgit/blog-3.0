#ifndef __TIM2_H
#define __TIM2_H

#include "stm32f10x.h" // 或你的 HAL/StdPeriph 头文件

// 定时器触发标志（全局变量）
extern volatile uint8_t sendFlag;

// TIM2 初始化函数
// arr: 自动重装载值
// psc: 预分频值
void TIM2_Init(uint16_t arr, uint16_t psc);

#endif
