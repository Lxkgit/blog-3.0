#include "stm32f10x.h"                  // Device header
#include "delay.h"
#include "constants.h"
#include "mqtt.h"
#include <stdio.h>
#include "usart.h"


int main(void) {

	char str[1024];
//	uart3_Init(115200);
	MQTT_Init();
	while(1) {
		Delay_s(5);
		sprintf(str,"aithinker");
		MQTT_Send_String(MQTT_PUB_TOPIC, str);
	}
}
