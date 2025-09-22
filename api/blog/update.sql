CREATE TABLE task_log
(
    id                 INT AUTO_INCREMENT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_code          VARCHAR(64) DEFAULT NULL COMMENT '任务唯一编码',
    child_task_code    VARCHAR(64) COMMENT '子任务唯一编码',
    task_uuid          VARCHAR(64) DEFAULT NULL COMMENT '任务执行流水号',
    task_log_type      TINYINT COMMENT '任务日志类型 1 发起任务日志 2 响应任务日志',
    index_count        INT         DEFAULT NULL  COMMENT '当前执行次数',
    task_count         INT         DEFAULT NULL  COMMENT '任务执行总次数，-1表示无限执行',
    task_result_status TINYINT COMMENT '任务执行结果：0失败，1成功',
    task_result        TEXT COMMENT '任务执行返回结果',
    error_msg          TEXT COMMENT '任务执行异常报错信息',
    start_time         DATETIME COMMENT '开始时间',
    end_time           DATETIME COMMENT '结束时间',
    create_time        DATETIME    DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    update_time        DATETIME    DEFAULT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    INDEX              idx_task_code (task_code),
    INDEX              idx_child_task_code (child_task_code),
    INDEX              idx_task_uuid (task_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行记录表';