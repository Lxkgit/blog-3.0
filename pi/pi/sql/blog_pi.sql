/*
 Navicat Premium Data Transfer

 Source Server         : 123.207.202.131
 Source Server Type    : MySQL
 Source Server Version : 80020
 Source Host           : 123.207.202.131:3306
 Source Schema         : blog_pi

 Target Server Type    : MySQL
 Target Server Version : 80020
 File Encoding         : 65001

 Date: 29/05/2025 12:58:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for blog_file_sync
-- ----------------------------
DROP TABLE IF EXISTS `blog_file_sync`;
CREATE TABLE `blog_file_sync`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int(0) NULL DEFAULT NULL COMMENT '用户id',
  `file_code` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '文件编码（同一文件与服务器中编码相同）',
  `local_file_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '本地文件目录',
  `local_file_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '本地文件名称',
  `service_file_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务器文件目录',
  `service_file_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '服务器文件名称',
  `file_size` int(0) NULL DEFAULT NULL COMMENT '文件大小（单位kb）',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '博客服务器文件同步表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blog_file_sync
-- ----------------------------

-- ----------------------------
-- Table structure for register_setting
-- ----------------------------
DROP TABLE IF EXISTS `register_setting`;
CREATE TABLE `register_setting`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `setting_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '配置名称',
  `setting_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '设置使用环境 dev：测试环境  pro：正式环境',
  `setting` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT ' json格式配置数据',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '注册配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of register_setting
-- ----------------------------
INSERT INTO `register_setting` VALUES (1, 'netty', 'dev', '{\r\n	\"ip\": \"localhost\",\r\n	\"port\": 9092,\r\n	\"registerId\": \"SMP1\",\r\n	\"username\": \"gszero\"\r\n}');
INSERT INTO `register_setting` VALUES (2, 'netty', 'pro', '{\r\n	\"ip\": \"49.232.129.253\",\r\n	\"port\": 60032,\r\n	\"registerId\": \"SMP1\",\r\n	\"username\": \"gszero\"\r\n}');
INSERT INTO `register_setting` VALUES (3, 'netty', 'test', '{\r\n	\"ip\": \"localhost\",\r\n	\"port\": 60032,\r\n	\"registerId\": \"SMP1\",\r\n	\"username\": \"gszero\"\r\n}');
INSERT INTO `register_setting` VALUES (4, 'ftp', 'pro', '{\r\n	\"ip\": \"49.232.129.253\",\r\n	\"port\": 61121,\r\n	\"username\": \"system\",\r\n	\"password\": \"Ftp@Admin123*.\",\r\n	\"basePath\": \"/opt/files\"\r\n}');
INSERT INTO `register_setting` VALUES (5, 'mqtt', 'dev', '{\r\n	\"ip\": \"localhost\",\r\n	\"port\": 1883,\r\n	\"username\": \"admin\",\r\n	\"password\": \"public\",\r\n	\"clientId\": \"SMP1\"\r\n}');
INSERT INTO `register_setting` VALUES (6, 'mqtt', 'pro', '{\r\n	\"ip\": \"172.18.0.4\",\r\n	\"port\": 1883,\r\n	\"username\": \"admin\",\r\n	\"password\": \"public\",\r\n	\"clientId\": \"SMP1\"\r\n}');
INSERT INTO `register_setting` VALUES (8, 'ftp', 'dev', '{\r\n	\"ip\": \"124.221.12.158\",\r\n	\"port\": 61121,\r\n	\"username\": \"system\",\r\n	\"password\": \"Ftp@Admin123*.\",\r\n	\"basePath\": \"D:/files\"\r\n}');

-- ----------------------------
-- Table structure for sensor_data
-- ----------------------------
DROP TABLE IF EXISTS `sensor_data`;
CREATE TABLE `sensor_data`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `chip_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '单片机芯片编号',
  `sensor_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '传感器型号',
  `data` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'mqtt 接收的数据',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '接收消息时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 802 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '传感器数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sensor_data
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
