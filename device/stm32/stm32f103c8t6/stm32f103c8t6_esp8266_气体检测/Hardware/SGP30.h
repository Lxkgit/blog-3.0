#ifndef __SGP30_H
#define __SGP30_H

#include "stm32f10x.h"

// SGP30 I2C 地址（左移一位）
#define SGP30_ADDR      0x58  

// I2C2 初始化函数
void I2C2_Init(void);


void SGP30_WriteReg(uint8_t RegAddress, uint8_t Data);

uint8_t SGP30_ReadReg(uint8_t RegAddress);


#endif
