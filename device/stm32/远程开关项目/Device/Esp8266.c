#include "esp8266.h"
#include "delay.h"
#include "usart.h"
#include <stdarg.h>
#include "constants.h"

struct STRUCT_USART_Fram ESP8266_Fram_Record_Struct = { 0 };

void ESP8266_Init() {
    usart2_Init(); 
}

/**
 * 对ESP8266模块发送AT指令
 * cmd：待发送的指令
 * ack1 ack2：期待的响应，为NULL表示不需要响应 二者为或逻辑关系
 * time：等待响应时间 单位是毫秒
 * 返回 1 发送成功 返回 2 发送失败
 */
bool ESP8266_Send_AT_Cmd(char *cmd, char *ack1, char *ack2, u32 time) {
    ESP8266_Fram_Record_Struct.InfBit.FramLength = 0; // 重新接受新的数据
    // ESP8266_USART("%s\r\n", cmd);
    if(ack1==0 && ack2==0) {
		return true;
    }
    Delay_ms(time);
    ESP8266_Fram_Record_Struct.Data_RX_BUF[ESP8266_Fram_Record_Struct.InfBit.FramLength] = '\0';
	
    printf("%s", ESP8266_Fram_Record_Struct.Data_RX_BUF);
    if(ack1!=0 && ack2!=0) {
        return ((bool) strstr(ESP8266_Fram_Record_Struct.Data_RX_BUF, ack1) || 
                (bool) strstr(ESP8266_Fram_Record_Struct.Data_RX_BUF, ack2));
    } else if(ack1 != 0)
        return ((bool) strstr(ESP8266_Fram_Record_Struct.Data_RX_BUF, ack1));
    else
        return ((bool) strstr(ESP8266_Fram_Record_Struct.Data_RX_BUF, ack2));
}


void ESP8266_AT_Test(void) {
    char count=0;
    Delay_ms(1000); 
    while(count < 10) {
        if(ESP8266_Send_AT_Cmd("AT+RESTORE","OK",NULL,500)) {
            printf("OK\r\n");
            return;
        }
        ++count;
    }
}

/**
 * 设置ESP8266的工作模式
 * 成功返回true 失败返回false
 */
bool ESP8266_Mode() {
    return ESP8266_Send_AT_Cmd("AT+CWMODE=1", "OK", "no change", 2500); 
}

/**
 * 配置mqtt用户属性
 * pClient_Id： mqtt注册id
 * pUserName：mqtt用户名
 * PassWord：mqtt登陆密码
 */
bool ESP8266_MQTTUSERCFG(char *pClient_Id, char *pUserName,char *PassWord) {
    char cCmd[120];
    sprintf(cCmd, "AT+MQTTUSERCFG=0,1,\"%s\",\"%s\",\"%s\",0,0,\"\"", pClient_Id, pUserName, PassWord);
    return ESP8266_Send_AT_Cmd(cCmd, "OK", NULL, 500);
}

/**
 * ESP8266连接wifi
 * pSSID：WiFi账号
 * pPassWord： WiFi密码
 * 成功返回true 失败返回false
 */
bool ESP8266_WiFi_Connect(char *pSSID, char *pPassWord) {
    char cmd[120];
    sprintf(cmd, "AT+CWJAP=\"%s\",\"%s\"", pSSID, pPassWord);
    return ESP8266_Send_AT_Cmd(cmd, "OK", NULL, 5000);
}

/**
 * esp8266连接mqtt
 * ip：WiFi账号
 * port： WiFi密码
 * 成功返回true 失败返回false
 */
bool ESP8266_MQTT_Connect(char *ip, int port) {
    char cmd[120];
    sprintf(cmd,"AT+MQTTCONN=0,\"%s\",%d,0", ip, port);
    return ESP8266_Send_AT_Cmd(cmd, "OK", NULL, 500 );
}

/**
 * 测试mqtt连接是否正常
 * 成功返回true 失败返回false
 */
bool ESP8266_MQTT_Connect_Test() {
    char cmd[120];
    sprintf(cmd, "AT+MQTTCONNECT=0,\"%s\",%d,\"%s\",\"%s\",\"%s\",60,1", 
			MQTT_IP, MQTT_PORT, MQTT_CLIENT_ID, MQTT_USERNAME, MQTT_PASSWORD);
    return ESP8266_Send_AT_Cmd(cmd, "OK", NULL, 1000);
}


/**
 * 订阅mqtt主题
 * topic：mqtt主题
 * 成功返回true 失败返回false
 */
bool ESP8266_MQTT_Sub(char *topic) {
    char cmd[120];
    sprintf(cmd, "AT+MQTTSUB=0,\"%s\",1", topic);
    return ESP8266_Send_AT_Cmd(cmd, "OK", NULL, 500 );
}

/**
 * ESP8266发送数据至mqtt服务器
 * topic：mqtt中topic
 * str：发布消息内容
 */
bool ESP8266_MQTT_Pub(char *topic, char *str) {
    char cmd[120];
    sprintf(cmd, "AT+MQTTPUB=0,\"%s\",\"%s\",1,0", topic, str);
    return ESP8266_Send_AT_Cmd(cmd, "OK", NULL, 1000);
}












