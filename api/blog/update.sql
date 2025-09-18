alter table article
    add article_file varchar(1000) DEFAULT NULL COMMENT '文章附件链接';

CREATE TABLE `task_param`
(
    `id`              int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`         int                                                           DEFAULT NULL COMMENT '用户id',
    `task_code`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '主任务编码',
    `child_task_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL COMMENT '子任务编码',
    `param_clazz`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '参数类',
    `param_json`      varchar(500)                                                  DEFAULT NULL COMMENT 'json格式任务执行参数',
    `task_count`      int                                                           DEFAULT NULL COMMENT '任务执行次数',
    `task_cron`       varchar(20)                                                   DEFAULT NULL COMMENT '任务执行cron表达式',
    `task_time`       varchar(20)                                                   DEFAULT NULL COMMENT '任务启动时间，秒不支持小数',
    `task_status`     tinyint                                                       DEFAULT NULL COMMENT '任务状态 1待执行 2进行中 3已结束',
    `create_time`     datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_time`     datetime                                                      DEFAULT NULL COMMENT '最近修改时间',
    PRIMARY KEY (`id`),
    KEY               `idx_task_uuid` (`task_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务参数表';

INSERT INTO `blog_file`.`task_param`(`id`, `user_id`, `task_code`, `child_task_code`, `param_clazz`, `param_json`,
                                     `task_count`, `task_cron`, `task_time`, `task_status`, `create_time`,
                                     `update_time`)
VALUES (1, 1, 'blog-system-task-sync-blog-file', 'system-sync-blog-file', NULL, NULL, -1, '0 0 0 * * *', NULL, 1,
        '2025-09-16 22:34:20', '2025-09-16 22:34:23');
INSERT INTO `blog_file`.`task_param`(`id`, `user_id`, `task_code`, `child_task_code`, `param_clazz`, `param_json`,
                                     `task_count`, `task_cron`, `task_time`, `task_status`, `create_time`,
                                     `update_time`)
VALUES (2, 1, 'blog-system-task-sync-device-file', 'system-sync-device-file',
        '[\"com.blog.core.domain.file.task.bo.SyncDeviceFileBo\"]',
        '[{ \"minioPath\": \"/user/img\", \"devicePath\": \"/mnt/test\", \"count\": 10 }]', -1, '0 0 0 * * *', NULL, 1,
        '2025-09-16 22:34:26', '2025-09-16 22:34:31');

