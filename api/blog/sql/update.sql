ALTER TABLE `blog_file`.`task_log`
    ADD COLUMN `task_id` int NULL COMMENT 'task_param表id' AFTER `id`;

ALTER TABLE `blog_file`.`task_log`
    ADD COLUMN `task_param` varchar(1024) NULL COMMENT '任务参数' AFTER `task_code`;

ALTER TABLE `blog_file`.`task_log`
DROP COLUMN `child_task_code`;