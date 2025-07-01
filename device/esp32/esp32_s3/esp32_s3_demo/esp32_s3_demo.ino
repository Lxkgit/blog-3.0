#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>

// Wi-Fi配置
const char *ssid = "TP-LINK_424D";
const char *password = "tplink96012";

const char *mqtt_server = "192.168.163.28";
const int mqtt_port = 1883;
const char *client_id = "ESP32-S3";
const char *mqtt_username = "admin";
const char *mqtt_password = "public";

// 主题配置
const char *sub_topic = "/sub/topic";
const char *pub_topic = "/pub/topic";

WiFiClient espClient;
PubSubClient client(espClient);
unsigned long lastMsg = 0;

// 连接Wi-Fi
void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("正在连接到Wi-Fi: ");
  Serial.println(ssid);

  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("Wi-Fi连接成功");
  Serial.print("IP地址: ");
  Serial.println(WiFi.localIP());
}

// MQTT消息回调函数
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("收到消息，主题: ");
  Serial.println(topic);
  
  // 将有效载荷转换为字符串
  String message;
  for (int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  Serial.print("消息内容: ");
  Serial.println(message);
  
  // 解析JSON
  DynamicJsonDocument doc(1024);
  deserializeJson(doc, message);
  
  // // 处理LED控制命令
  // if (doc.containsKey("params") && doc["params"].containsKey("LightSwitch")) {
  //   int lightSwitch = doc["params"]["LightSwitch"];
  //   Serial.print("LED状态: ");
  //   Serial.println(lightSwitch);
    

    
  //   // 发布状态更新
  //   reportStatus(lightSwitch);
  // }
}

// 重连MQTT服务器
void reconnect() {
  while (!client.connected()) {
    Serial.print("尝试MQTT连接...");
    if (client.connect(client_id, mqtt_username, mqtt_password)) {
      Serial.println("连接成功");
      
      // 订阅主题
      client.subscribe(sub_topic);
      Serial.print("已订阅主题: ");
      Serial.println(sub_topic);
      
    } else {
      Serial.print("连接失败, 错误码= ");
      Serial.print(client.state());
      Serial.println(" 5秒后重试...");
      delay(5000);
    }
  }
}

// 上报LED状态
void reportStatus(int status) {
  DynamicJsonDocument doc(256);
  
  doc["id"] = String(millis());
  doc["version"] = "1.0";
  doc["method"] = "thing.event.property.post";
  
  JsonObject params = doc.createNestedObject("params");
  params["LightSwitch"] = status;
  
  String output;
  serializeJson(doc, output);
  
  Serial.print("发布状态: ");
  Serial.println(output);
  
  client.publish(pub_topic, output.c_str());
}

void setup() {
  // 初始化串口
  Serial.begin(115200);

  // 连接Wi-Fi
  setup_wifi();
  
  // 设置MQTT服务器
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);
}

void loop() {
  // 确保MQTT客户端连接
  if (!client.connected()) {
    reconnect();
  }
  client.loop();
  
  // 定期上报状态（每60秒）
  unsigned long now = millis();
  if (now - lastMsg > 60000) {
    lastMsg = now;
    // reportStatus(digitalRead(LED_PIN));
  }
}
