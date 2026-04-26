USE `campus_platform`;

-- 先准备几个演示用户（若已存在则忽略）
INSERT IGNORE INTO `user` (`openid`, `phone`, `nickname`, `avatar_url`, `role`, `status`)
VALUES
('demo_openid_1001', '13800001001', '小李同学', 'https://example.com/u1.png', 1, 1),
('demo_openid_1002', '13800001002', '小王同学', 'https://example.com/u2.png', 1, 1),
('demo_openid_1003', '13800001003', '小张同学', 'https://example.com/u3.png', 1, 1);

-- 失物招领（type=1）演示数据
INSERT INTO `post`
(`user_id`, `type`, `title`, `description`, `category`, `price`, `location`, `lost_found_time`, `lost_status`, `images`, `contact`, `status`, `view_count`, `like_count`, `comment_count`, `report_count`)
VALUES
(
  (SELECT id FROM `user` WHERE phone='13800001001' LIMIT 1),
  1,
  '图书馆丢失校园卡，姓名李同学',
  '今天上午在图书馆三楼自习区丢失校园卡，如有拾到请联系我，非常感谢。',
  '证件',
  NULL,
  '图书馆三楼自习区',
  '2026-04-05 10:20:00',
  1,
  '["http://localhost:8080/ssm/api/upload/file/demo_lost_1.jpg"]',
  '微信:li_2026',
  1,
  23,
  5,
  2,
  0
),
(
  (SELECT id FROM `user` WHERE phone='13800001002' LIMIT 1),
  1,
  '在一食堂捡到蓝色保温杯',
  '中午12点左右在一食堂二楼靠窗位置捡到一个蓝色保温杯，请失主联系描述细节认领。',
  '生活用品',
  NULL,
  '一食堂二楼',
  '2026-04-05 12:05:00',
  2,
  '["http://localhost:8080/ssm/api/upload/file/demo_found_1.jpg"]',
  '手机号:13800001002',
  1,
  15,
  3,
  1,
  0
),
(
  (SELECT id FROM `user` WHERE phone='13800001003' LIMIT 1),
  1,
  '操场看台附近遗失黑色雨伞',
  '昨晚跑步后把雨伞落在看台，伞柄有白色贴纸，拾到请私信。',
  '生活用品',
  NULL,
  '东操场看台',
  '2026-04-04 20:40:00',
  1,
  '["http://localhost:8080/ssm/api/upload/file/demo_lost_2.jpg"]',
  'QQ:123456789',
  1,
  9,
  1,
  0,
  0
);

-- 二手物品（type=2）演示数据
INSERT INTO `post`
(`user_id`, `type`, `title`, `description`, `category`, `price`, `location`, `lost_found_time`, `lost_status`, `images`, `contact`, `status`, `view_count`, `like_count`, `comment_count`, `report_count`)
VALUES
(
  (SELECT id FROM `user` WHERE phone='13800001001' LIMIT 1),
  2,
  '转让九成新高数教材（同济版）',
  '书内少量划线，不影响使用，可当面验书。',
  '书籍',
  22.00,
  NULL,
  NULL,
  NULL,
  '["http://localhost:8080/ssm/api/upload/file/demo_sale_book_1.jpg"]',
  '微信:math_book_01',
  1,
  40,
  8,
  4,
  0
),
(
  (SELECT id FROM `user` WHERE phone='13800001002' LIMIT 1),
  2,
  '二手小米充电宝 10000mAh',
  '使用半年，功能正常，附原装线。',
  '电子产品',
  35.00,
  NULL,
  NULL,
  NULL,
  '["http://localhost:8080/ssm/api/upload/file/demo_sale_digital_1.jpg"]',
  '手机号:13800001002',
  1,
  31,
  6,
  3,
  0
),
(
  (SELECT id FROM `user` WHERE phone='13800001003' LIMIT 1),
  2,
  '毕业清仓：宿舍收纳箱两个',
  '8成新，容量大，打包带走更优惠。',
  '生活用品',
  18.00,
  NULL,
  NULL,
  NULL,
  '["http://localhost:8080/ssm/api/upload/file/demo_sale_box_1.jpg"]',
  '微信:room_box_03',
  1,
  12,
  2,
  1,
  0
);

-- 额外：插入一条待审核帖子，方便管理员演示审核
INSERT INTO `post`
(`user_id`, `type`, `title`, `description`, `category`, `price`, `location`, `lost_found_time`, `lost_status`, `images`, `contact`, `status`, `view_count`, `like_count`, `comment_count`, `report_count`)
VALUES
(
  (SELECT id FROM `user` WHERE phone='13800001001' LIMIT 1),
  2,
  '待审核演示：二手台灯',
  '台灯可调亮度，功能正常。',
  '生活用品',
  15.00,
  NULL,
  NULL,
  NULL,
  '["http://localhost:8080/ssm/api/upload/file/demo_pending_1.jpg"]',
  '微信:lamp_demo',
  0,
  0,
  0,
  0,
  0
);

-- 创建收藏表
CREATE TABLE IF NOT EXISTS `post_collect` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL COMMENT '用户ID',
  `post_id` INT NOT NULL COMMENT '帖子ID',
  `create_time` DATETIME NOT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏表';
