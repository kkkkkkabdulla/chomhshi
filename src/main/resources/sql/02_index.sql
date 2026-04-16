USE `campus_platform`;

ALTER TABLE `admin`
  ADD UNIQUE KEY `uk_admin_username` (`username`);

ALTER TABLE `user`
  ADD INDEX `idx_user_phone` (`phone`),
  ADD INDEX `idx_user_openid` (`openid`);

ALTER TABLE `post`
  ADD INDEX `idx_post_user_id` (`user_id`),
  ADD INDEX `idx_post_type` (`type`),
  ADD INDEX `idx_post_status` (`status`),
  ADD INDEX `idx_post_create_time` (`create_time`),
  ADD INDEX `idx_post_report_count` (`report_count`);

ALTER TABLE `user_token`
  ADD INDEX `idx_token_user_id` (`user_id`),
  ADD INDEX `idx_token_token` (`token`);

ALTER TABLE `post_like`
  ADD UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  ADD INDEX `idx_like_post_id` (`post_id`),
  ADD INDEX `idx_like_user_id` (`user_id`);

ALTER TABLE `post_comment`
  ADD INDEX `idx_comment_post_id` (`post_id`),
  ADD INDEX `idx_comment_user_id` (`user_id`),
  ADD INDEX `idx_comment_parent_id` (`parent_id`);

ALTER TABLE `post_report`
  ADD INDEX `idx_report_post_id` (`post_id`),
  ADD INDEX `idx_report_reporter_id` (`reporter_id`),
  ADD INDEX `idx_report_status` (`status`),
  ADD INDEX `idx_report_created_at` (`created_at`);

ALTER TABLE `sensitive_word`
  ADD UNIQUE KEY `uk_sensitive_word` (`word`);
