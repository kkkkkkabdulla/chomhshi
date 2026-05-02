USE campus_platform;

ALTER TABLE `user`
    ADD COLUMN `ban_reason` VARCHAR(200) DEFAULT NULL COMMENT '封禁原因' AFTER `status`,
    ADD COLUMN `ban_remark` VARCHAR(500) DEFAULT NULL COMMENT '封禁备注' AFTER `ban_reason`;
