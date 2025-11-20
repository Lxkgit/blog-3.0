//#include "tim2.h"

//// 定时器触发标志
//volatile uint8_t sendFlag = 0;

//// TIM2 中断服务函数
//void TIM2_IRQHandler(void)
//{
//    if (TIM_GetITStatus(TIM2, TIM_IT_Update) != RESET)
//    {
//        TIM_ClearITPendingBit(TIM2, TIM_IT_Update);
//        sendFlag = 1; // 定时器到期，设置标志
//    }
//}

//// TIM2 初始化函数
//void TIM2_Init(uint16_t arr, uint16_t psc)
//{
//    TIM_TimeBaseInitTypeDef TIM_TimeBaseStructure;
//    NVIC_InitTypeDef NVIC_InitStructure;

//    RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2, ENABLE);

//    TIM_TimeBaseStructure.TIM_Period = arr;
//    TIM_TimeBaseStructure.TIM_Prescaler = psc;
//    TIM_TimeBaseStructure.TIM_ClockDivision = 0;
//    TIM_TimeBaseStructure.TIM_CounterMode = TIM_CounterMode_Up;

//    TIM_TimeBaseInit(TIM2, &TIM_TimeBaseStructure);
//    TIM_ITConfig(TIM2, TIM_IT_Update, ENABLE);
//    TIM_Cmd(TIM2, ENABLE);

//    // 中断优先级配置
//    NVIC_InitStructure.NVIC_IRQChannel = TIM2_IRQn;
//    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
//    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
//    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
//    NVIC_Init(&NVIC_InitStructure);
//}
