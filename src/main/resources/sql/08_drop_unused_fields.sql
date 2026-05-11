USE campus_platform;

ALTER TABLE `user`
    DROP COLUMN `role`,
    DROP COLUMN `wx_avatar`;

ALTER TABLE `post`
    DROP COLUMN `view_count`,
    DROP COLUMN `report_count`,
    DROP COLUMN `lost_found_time`,
    DROP COLUMN `lost_status`;

DROP INDEX `idx_post_report_count` ON `post`;
