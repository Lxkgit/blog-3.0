#include "esp_camera.h"
#include <WiFi.h>

// ========== 选择摄像头型号 ==========
#define CAMERA_MODEL_AI_THINKER
#include "camera_pins.h"

// ========== WiFi 配置 ==========
const char *ssid = "TP-LINK_424D";
const char *password = "tplink96012";

void startCameraServer();
void setupLedFlash(int pin);

void setup() {
  Serial.begin(115200);
  Serial.setDebugOutput(true);
  Serial.println("\n[系统] 启动ESP32-CAM...");

  // ====== 配置摄像头参数 ======
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sccb_sda = SIOD_GPIO_NUM;
  config.pin_sccb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.frame_size = FRAMESIZE_UXGA;    // UXGA(1600x1200)
  config.pixel_format = PIXFORMAT_JPEG;  // 使用 JPEG 格式（适合网络传输）
  config.grab_mode = CAMERA_GRAB_WHEN_EMPTY;
  config.fb_location = CAMERA_FB_IN_PSRAM;
  config.jpeg_quality = 12;
  config.fb_count = 1;

  // ====== 根据是否有 PSRAM 进行优化 ======
  if (config.pixel_format == PIXFORMAT_JPEG) {
    if (psramFound()) {
      config.jpeg_quality = 10;  // 更高质量
      config.fb_count = 2;       // 双缓冲更流畅
      config.grab_mode = CAMERA_GRAB_LATEST;
      Serial.println("[摄像头] 检测到PSRAM，启用高质量模式");
    } else {
      config.frame_size = FRAMESIZE_SVGA;
      config.fb_location = CAMERA_FB_IN_DRAM;
      Serial.println("[摄像头] 未检测到PSRAM，降级到低分辨率模式");
    }
  }

  // ESP-EYE 板子上的 IO 配置
#if defined(CAMERA_MODEL_ESP_EYE)
  pinMode(13, INPUT_PULLUP);
  pinMode(14, INPUT_PULLUP);
#endif

  // ====== 初始化摄像头 ======
  Serial.println("[摄像头] 正在初始化...");
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("[错误] 摄像头初始化失败，错误代码 0x%x\n", err);
    return;
  }
  Serial.println("[摄像头] 初始化成功");

  // ====== 获取传感器对象并调整参数 ======
  sensor_t *s = esp_camera_sensor_get();
  if (s->id.PID == OV3660_PID) {
    s->set_vflip(s, 1);        // 图像翻转
    s->set_brightness(s, 1);   // 提亮
    s->set_saturation(s, -2);  // 降低饱和度
  }
  if (config.pixel_format == PIXFORMAT_JPEG) {
    s->set_framesize(s, FRAMESIZE_QVGA);  // 默认使用 QVGA 提高流畅度
  }

  // ====== 启用LED闪光灯（如果定义了LED引脚） ======
#if defined(LED_GPIO_NUM)
  setupLedFlash(LED_GPIO_NUM);
  Serial.println("[LED] 闪光灯初始化完成");
#endif

  // ====== 连接WiFi ======
  Serial.printf("[WiFi] 正在连接 %s...\n", ssid);
  WiFi.begin(ssid, password);
  WiFi.setSleep(false);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n[WiFi] 已连接成功");
  Serial.print("[WiFi] IP 地址: ");
  Serial.println(WiFi.localIP());

  // ====== 启动摄像头WebServer ======
  startCameraServer();
  Serial.println("[系统] 摄像头服务已启动");
  Serial.printf("[系统] 打开浏览器访问: http://%s\n", WiFi.localIP().toString().c_str());
}

void loop() {
  delay(10000);  // 主循环不用做事，WebServer独立运行
}
