USE `campus_platform`;

INSERT INTO `admin` (`username`, `password`, `status`)
VALUES ('admin', 'admin123', 1)
ON DUPLICATE KEY UPDATE `status`=VALUES(`status`);

INSERT IGNORE INTO `sensitive_word` (`word`, `level`) VALUES
('诈骗', 2),
('色情', 2),
('赌博', 2),
('代考', 2),
('作弊器', 2),
('枪支', 2),
('毒品', 2),
('洗钱', 2),
('辱骂', 1),
('引战', 1),
('约架', 1),
('外挂', 1),
('灰产', 2),
('非法', 1),
('兼职刷单', 2),
('传销', 2),
('非法集资', 2),
('虚假中奖', 2),
('钓鱼链接', 2),
('恶意广告', 1);

INSERT INTO `user` (`openid`, `phone`, `nickname`, `avatar_url`, `role`, `status`)
VALUES ('test_openid_001', '13800000001', '测试用户A', 'https://example.com/a.png', 1, 1)
ON DUPLICATE KEY UPDATE `nickname`=VALUES(`nickname`);
