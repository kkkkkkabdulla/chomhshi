USE `campus_platform`;

-- 迁移旧举报表结构到新结构（若你是新库可跳过）
-- 说明：旧 reason 为文本时，直接改为 TINYINT 会报错，所以先用临时列做映射

-- 1) 先补临时列并映射 reason 文本 -> reason_type_tmp
ALTER TABLE `post_report`
  ADD COLUMN `reason_type_tmp` TINYINT NULL AFTER `post_id`;

UPDATE `post_report`
SET `reason_type_tmp` = CASE
    WHEN reason IS NULL OR reason = '' THEN 4
    WHEN reason REGEXP '^[0-9]+$' THEN CAST(reason AS UNSIGNED)
    WHEN reason IN ('违规内容','虚假信息') THEN 1
    WHEN reason IN ('广告骚扰','广告') THEN 2
    WHEN reason IN ('色情低俗','色情','低俗') THEN 3
    WHEN reason IN ('其他') THEN 4
    ELSE 4
END;

-- 2) 结构变更（把旧字段改名/改类型）
ALTER TABLE `post_report`
  CHANGE COLUMN `user_id` `reporter_id` INT NOT NULL,
  DROP COLUMN `reason`,
  CHANGE COLUMN `description` `reason_desc` VARCHAR(500) NULL,
  CHANGE COLUMN `admin_id` `handled_by` INT NULL,
  CHANGE COLUMN `handle_time` `handled_at` DATETIME NULL,
  CHANGE COLUMN `create_time` `created_at` DATETIME NULL,
  ADD COLUMN `admin_remark` VARCHAR(200) NULL AFTER `status`;

-- 3) 用临时列填充新 reason_type，并清理
ALTER TABLE `post_report`
  ADD COLUMN `reason_type` TINYINT NOT NULL DEFAULT 4 AFTER `post_id`;

UPDATE `post_report`
SET `reason_type` = CASE
    WHEN reason_type_tmp IN (1,2,3,4) THEN reason_type_tmp
    ELSE 4
END;

ALTER TABLE `post_report`
  DROP COLUMN `reason_type_tmp`;

-- 4) 索引重建（容错：索引存在才删除）
SET @sql1 = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'post_report'
        AND index_name = 'uk_report_post_user'
    ),
    'ALTER TABLE `post_report` DROP INDEX `uk_report_post_user`',
    'SELECT 1'
  )
);
PREPARE stmt1 FROM @sql1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

SET @sql2 = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.statistics
      WHERE table_schema = DATABASE()
        AND table_name = 'post_report'
        AND index_name = 'idx_report_user_id'
    ),
    'ALTER TABLE `post_report` DROP INDEX `idx_report_user_id`',
    'SELECT 1'
  )
);
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 5) 新索引（存在同名索引会报错，执行前可先 SHOW INDEX 检查）
ALTER TABLE `post_report`
  ADD INDEX `idx_report_reporter_id` (`reporter_id`),
  ADD INDEX `idx_report_created_at` (`created_at`),
  ADD INDEX `idx_report_status` (`status`);

DESC post_report;
SELECT id, reporter_id, post_id, reason_type, status, admin_remark, handled_by, created_at, handled_at
FROM post_report
LIMIT 10;