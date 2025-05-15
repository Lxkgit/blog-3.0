/*
 Navicat Premium Data Transfer

 Source Server         : 49.232.129.253
 Source Server Type    : MySQL
 Source Server Version : 80020
 Source Host           : 49.232.129.253:3306
 Source Schema         : blog_auth

 Target Server Type    : MySQL
 Target Server Version : 80020
 File Encoding         : 65001

 Date: 15/05/2025 12:57:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oauth2_authorization
-- ----------------------------
DROP TABLE IF EXISTS `oauth2_authorization`;
CREATE TABLE `oauth2_authorization`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '令牌ID',
  `registered_client_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '注册的客户端ID',
  `principal_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主体名称',
  `authorization_grant_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '授权授予类型',
  `authorized_scopes` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '授权范围',
  `attributes` blob NULL COMMENT '属性',
  `state` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '状态',
  `authorization_code_value` blob NULL COMMENT '授权码值',
  `authorization_code_issued_at` timestamp(0) NULL DEFAULT NULL COMMENT '授权码颁发时间',
  `authorization_code_expires_at` timestamp(0) NULL DEFAULT NULL COMMENT '授权码过期时间',
  `authorization_code_metadata` blob NULL COMMENT '授权码元数据',
  `access_token_value` blob NULL COMMENT '访问令牌值',
  `access_token_issued_at` timestamp(0) NULL DEFAULT NULL COMMENT '访问令牌颁发时间',
  `access_token_expires_at` timestamp(0) NULL DEFAULT NULL COMMENT '访问令牌过期时间',
  `access_token_metadata` blob NULL COMMENT '访问令牌元数据',
  `access_token_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问令牌类型',
  `access_token_scopes` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '访问令牌范围',
  `oidc_id_token_value` blob NULL COMMENT 'OIDC ID 令牌值',
  `oidc_id_token_issued_at` timestamp(0) NULL DEFAULT NULL COMMENT 'OIDC ID 令牌颁发时间',
  `oidc_id_token_expires_at` timestamp(0) NULL DEFAULT NULL COMMENT 'OIDC ID 令牌过期时间',
  `oidc_id_token_metadata` blob NULL COMMENT 'OIDC ID 令牌元数据',
  `refresh_token_value` blob NULL COMMENT '刷新令牌值',
  `refresh_token_issued_at` timestamp(0) NULL DEFAULT NULL COMMENT '刷新令牌颁发时间',
  `refresh_token_expires_at` timestamp(0) NULL DEFAULT NULL COMMENT '刷新令牌过期时间',
  `refresh_token_metadata` blob NULL COMMENT '刷新令牌元数据',
  `user_code_value` blob NULL COMMENT '用户代码值',
  `user_code_issued_at` timestamp(0) NULL DEFAULT NULL COMMENT '用户代码颁发时间',
  `user_code_expires_at` timestamp(0) NULL DEFAULT NULL COMMENT '用户代码过期时间',
  `user_code_metadata` blob NULL COMMENT '用户代码元数据',
  `device_code_value` blob NULL COMMENT '设备代码值',
  `device_code_issued_at` timestamp(0) NULL DEFAULT NULL COMMENT '设备代码颁发时间',
  `device_code_expires_at` timestamp(0) NULL DEFAULT NULL COMMENT '设备代码过期时间',
  `device_code_metadata` blob NULL COMMENT '设备代码元数据',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth2_authorization
-- ----------------------------

-- ----------------------------
-- Table structure for oauth2_authorization_consent
-- ----------------------------
DROP TABLE IF EXISTS `oauth2_authorization_consent`;
CREATE TABLE `oauth2_authorization_consent`  (
  `registered_client_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '注册的客户端ID',
  `principal_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主体名称',
  `authorities` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '授权',
  PRIMARY KEY (`registered_client_id`, `principal_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth2_authorization_consent
-- ----------------------------

-- ----------------------------
-- Table structure for oauth2_registered_client
-- ----------------------------
DROP TABLE IF EXISTS `oauth2_registered_client`;
CREATE TABLE `oauth2_registered_client`  (
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端ID',
  `client_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端ID',
  `client_id_issued_at` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '客户端ID颁发时间',
  `client_secret` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端密钥',
  `client_secret_expires_at` timestamp(0) NULL DEFAULT NULL COMMENT '客户端密钥过期时间',
  `client_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端名称',
  `client_authentication_methods` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端认证方法',
  `authorization_grant_types` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '授权授予类型',
  `redirect_uris` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重定向URI',
  `post_logout_redirect_uris` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注销后重定向URI',
  `scopes` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '授权范围',
  `client_settings` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户端设置',
  `token_settings` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '令牌设置',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oauth2_registered_client
-- ----------------------------
INSERT INTO `oauth2_registered_client` VALUES ('38e696ad-07d8-444d-8685-4b80ba3e5b75', 'kucun', '2023-12-25 22:06:23', '$2a$10$h4KOnpuDgzpptwQhTK09GOuH7LZ7mqU9K/rA3OfnBWS2HUrMYch.6', NULL, '库存项目', 'client_secret_basic', 'authorization_code', 'http://localhost:3002/callback', 'http://localhost:3002', 'openid', '{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.client.require-proof-key\":false,\"settings.client.require-authorization-consent\":false}', '{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.token.reuse-refresh-tokens\":true,\"settings.token.id-token-signature-algorithm\":[\"org.springframework.security.oauth2.jose.jws.SignatureAlgorithm\",\"RS256\"],\"settings.token.access-token-time-to-live\":[\"java.time.Duration\",3600.000000000],\"settings.token.access-token-format\":{\"@class\":\"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat\",\"value\":\"self-contained\"},\"settings.token.refresh-token-time-to-live\":[\"java.time.Duration\",3600.000000000],\"settings.token.authorization-code-time-to-live\":[\"java.time.Duration\",300.000000000]}');
INSERT INTO `oauth2_registered_client` VALUES ('fed76716-d0db-4f68-902c-f9d97d029efb', 'dianshang', '2023-12-25 22:06:15', '$2a$10$Y3OPnq3jYbxVuXQ04mkcEOgvwsozQ2E3Ky2T9ebTFylYjocwBLnDK', NULL, '电商项目', 'client_secret_basic', 'refresh_token,client_credentials,authorization_code', 'http://49.232.129.253/callback', 'http://localhost:3001', 'openid', '{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.client.require-proof-key\":false,\"settings.client.require-authorization-consent\":false}', '{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.token.reuse-refresh-tokens\":true,\"settings.token.id-token-signature-algorithm\":[\"org.springframework.security.oauth2.jose.jws.SignatureAlgorithm\",\"RS256\"],\"settings.token.access-token-time-to-live\":[\"java.time.Duration\",28800.000000000],\"settings.token.access-token-format\":{\"@class\":\"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat\",\"value\":\"self-contained\"},\"settings.token.refresh-token-time-to-live\":[\"java.time.Duration\",2592000.000000000],\"settings.token.authorization-code-time-to-live\":[\"java.time.Duration\",300.000000000]}');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` int(0) NULL DEFAULT NULL COMMENT '父级id',
  `menu_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单名称/按钮名称',
  `menu_icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '前端vue 跳转路径',
  `auth` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限名称',
  `menu_type` tinyint(0) NULL DEFAULT NULL COMMENT '类型  1:菜单 , 2:按钮',
  `sort` tinyint(0) NULL DEFAULT NULL COMMENT '排序序号',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_is_type`(`menu_type`) USING BTREE,
  INDEX `idx_auth`(`auth`) USING BTREE,
  INDEX `idx_path`(`menu_path`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 706 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统菜单/权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '用户管理', 'icon-people', '', 'sys:manage', 0, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (2, 0, '内容管理', 'icon-book', '', 'sys:content', 0, 2, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (3, 0, '文件管理', 'icon-file', '', 'sys:file', 0, 3, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (4, 0, '设备管理', 'icon-file', '', 'sys:device', 0, 5, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (5, 0, '系统设置', 'icon-setting', '', 'sys:setting', 0, 6, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (7, 0, '工具', 'icon-file', '', 'sys:util', 0, 4, '2025-05-10 11:00:14');
INSERT INTO `sys_menu` VALUES (50, 1, '角色管理', '', '/admin/role', 'sys:role', 1, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (51, 1, '用户管理', '', '/admin/user', 'sys:user', 1, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (60, 2, '文章管理', '', '/admin/article', 'sys:article', 1, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (61, 2, '文章分类', '', '/admin/article/type', 'sys:article:type', 1, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (62, 2, '文章标签', '', '/admin/article/label', 'sys:article:label', 1, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (63, 2, '日记管理', '', '/admin/diary', 'sys:diary', 1, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (64, 2, '文档管理', '', '/admin/doc', 'sys:doc', 1, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (69, 7, '日历', '', '/admin/tool/calendar', 'sys:tool', 1, 1, '2025-05-10 11:04:10');
INSERT INTO `sys_menu` VALUES (70, 3, '文件云盘', '', '/admin/file', 'sys:file:user', 1, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (80, 5, '网站设置', '', '/admin/setting/web', 'sys:setting:all', 1, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (81, 5, '个人设置', '', '/admin/setting/user', 'sys:setting:user', 1, 1, '2025-03-19 21:59:40');
INSERT INTO `sys_menu` VALUES (90, 4, '服务器设备', '', '/admin/device', 'sys:device:service', 1, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (100, 50, '角色列表', '', '', 'sys:role:select', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (101, 50, '添加角色', '', '', 'sys:role:insert', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (102, 50, '删除角色', '', '', 'sys:role:delete', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (103, 50, '修改角色', '', '', 'sys:role:update', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (104, 50, '查看角色权限', '', '', 'sys:role:permission:select', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (105, 50, '修改角色权限', '', '', 'sys:role:permission:update', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (200, 51, '用户列表', '', '', 'sys:user:list', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (201, 51, '注销用户', '', '', 'sys:user:delete', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (202, 51, '修改用户角色', '', '', 'sys:user:role:update', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (300, 60, '文章列表', '', '', 'sys:article:list', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (301, 60, '创建文章', '', '', 'sys:article:insert', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (302, 60, '修改文章', '', '', 'sys:article:update', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (303, 60, '删除文章', '', '', 'sys:article:delete', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (400, 61, '文章分类列表', '', '', 'sys:article:type:list', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (401, 61, '创建分类', '', '', 'sys:article:type:insert', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (402, 61, '修改分类', '', '', 'sys:article:type:update', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (403, 61, '删除分类', '', '', 'sys:article:type:delete', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (500, 62, '文章标签列表', '', '', 'sys:article:label:list', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (501, 62, '创建标签', '', '', 'sys:article:label:insert', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (502, 62, '修改标签', '', '', 'sys:article:label:update', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (503, 62, '删除标签', '', '', 'sys:article:label:delete', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (504, 62, '创建标签分类', '', '', 'sys:article:label:type:insert', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (505, 62, '修改标签分类', '', '', 'sys:article:label:type:update', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (506, 62, '删除标签分类', '', '', 'sys:article:label:type:delete', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (600, 63, '日记列表', '', '', 'sys:diary:list', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (601, 63, '创建日记', '', '', 'sys:diary:insert', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (602, 63, '修改日记', '', '', 'sys:diary:update', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (603, 63, '删除日记', '', '', 'sys:diary:delete', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (700, 64, '文档列表', '', '', 'sys:doc:list', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (701, 64, '文档目录列表', '', '', 'sys:doc:catalog:list', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (702, 64, '创建文档', '', '', 'sys:doc:insert', 2, 1, '2025-03-19 21:59:41');
INSERT INTO `sys_menu` VALUES (703, 64, '修改文档', '', '', 'sys:doc:update', 2, 1, '2025-03-19 21:59:41');

-- ----------------------------
-- Table structure for sys_menu_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu_role`;
CREATE TABLE `sys_menu_role`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `role_id` int(0) NULL DEFAULT NULL COMMENT '角色id',
  `menu_id` int(0) NULL DEFAULT NULL COMMENT '权限id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 678 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '权限角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu_role
-- ----------------------------
INSERT INTO `sys_menu_role` VALUES (574, 1, 1);
INSERT INTO `sys_menu_role` VALUES (576, 1, 2);
INSERT INTO `sys_menu_role` VALUES (577, 1, 3);
INSERT INTO `sys_menu_role` VALUES (578, 1, 4);
INSERT INTO `sys_menu_role` VALUES (579, 1, 5);
INSERT INTO `sys_menu_role` VALUES (580, 1, 50);
INSERT INTO `sys_menu_role` VALUES (581, 1, 51);
INSERT INTO `sys_menu_role` VALUES (582, 1, 60);
INSERT INTO `sys_menu_role` VALUES (583, 1, 61);
INSERT INTO `sys_menu_role` VALUES (584, 1, 62);
INSERT INTO `sys_menu_role` VALUES (585, 1, 63);
INSERT INTO `sys_menu_role` VALUES (586, 1, 64);
INSERT INTO `sys_menu_role` VALUES (587, 1, 70);
INSERT INTO `sys_menu_role` VALUES (588, 1, 80);
INSERT INTO `sys_menu_role` VALUES (589, 1, 81);
INSERT INTO `sys_menu_role` VALUES (590, 1, 90);
INSERT INTO `sys_menu_role` VALUES (591, 1, 100);
INSERT INTO `sys_menu_role` VALUES (592, 1, 101);
INSERT INTO `sys_menu_role` VALUES (593, 1, 102);
INSERT INTO `sys_menu_role` VALUES (594, 1, 103);
INSERT INTO `sys_menu_role` VALUES (595, 1, 104);
INSERT INTO `sys_menu_role` VALUES (596, 1, 105);
INSERT INTO `sys_menu_role` VALUES (597, 1, 200);
INSERT INTO `sys_menu_role` VALUES (598, 1, 201);
INSERT INTO `sys_menu_role` VALUES (599, 1, 202);
INSERT INTO `sys_menu_role` VALUES (600, 1, 300);
INSERT INTO `sys_menu_role` VALUES (601, 1, 301);
INSERT INTO `sys_menu_role` VALUES (602, 1, 302);
INSERT INTO `sys_menu_role` VALUES (603, 1, 303);
INSERT INTO `sys_menu_role` VALUES (604, 1, 400);
INSERT INTO `sys_menu_role` VALUES (605, 1, 401);
INSERT INTO `sys_menu_role` VALUES (606, 1, 402);
INSERT INTO `sys_menu_role` VALUES (607, 1, 403);
INSERT INTO `sys_menu_role` VALUES (608, 1, 500);
INSERT INTO `sys_menu_role` VALUES (609, 1, 501);
INSERT INTO `sys_menu_role` VALUES (610, 1, 502);
INSERT INTO `sys_menu_role` VALUES (611, 1, 503);
INSERT INTO `sys_menu_role` VALUES (612, 1, 504);
INSERT INTO `sys_menu_role` VALUES (613, 1, 505);
INSERT INTO `sys_menu_role` VALUES (614, 1, 506);
INSERT INTO `sys_menu_role` VALUES (615, 1, 600);
INSERT INTO `sys_menu_role` VALUES (616, 1, 601);
INSERT INTO `sys_menu_role` VALUES (617, 1, 602);
INSERT INTO `sys_menu_role` VALUES (618, 1, 603);
INSERT INTO `sys_menu_role` VALUES (619, 1, 700);
INSERT INTO `sys_menu_role` VALUES (620, 1, 701);
INSERT INTO `sys_menu_role` VALUES (621, 1, 702);
INSERT INTO `sys_menu_role` VALUES (622, 1, 703);
INSERT INTO `sys_menu_role` VALUES (623, 1, 704);
INSERT INTO `sys_menu_role` VALUES (624, 1, 705);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色编码',
  `role_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色名称',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建用户',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '修改用户',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'admin', '管理员', 'admin', '2025-03-23 22:52:21', 'admin', '2025-03-23 22:52:26');
INSERT INTO `sys_role` VALUES (2, 'user', '普通用户', 'z', '2025-04-26 19:17:33', NULL, NULL);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '密码',
  `nickname` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '姓名',
  `head_img` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户头像',
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱地址',
  `status` varchar(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '状态（1有效,0无效）',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '最近修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_account`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'gszero', '$2a$10$0nZBWnCqPASXP.cnA4vx7.yg3Bdxm4h8hGOZ2MDX25uFLQh/huui.', '张三', 'http://123.207.202.131:9000/blog/1/other/img/2025-04-29_22:18:27_189587_7.png', '470687917@qq.com', '1', '2023-12-15 04:37:51', NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `role_id` int(0) NULL DEFAULT NULL COMMENT '角色id',
  `user_id` int(0) NULL DEFAULT NULL COMMENT '用户id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
