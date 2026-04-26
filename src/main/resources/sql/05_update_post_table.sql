USE `campus_platform`;

-- 为post表添加收藏数量字段
ALTER TABLE `post`
ADD COLUMN `collect_count` INT NOT NULL DEFAULT 0 COMMENT '收藏数量' AFTER `report_count`;

-- 更新现有帖子的收藏数量为0
UPDATE `post` SET `collect_count` = 0;