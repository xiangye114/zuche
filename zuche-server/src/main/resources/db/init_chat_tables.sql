-- AI客服相关表
CREATE TABLE IF NOT EXISTS `chat_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(100) DEFAULT '新对话',
  `status` tinyint DEFAULT 1 COMMENT '1-进行中, 0-已结束',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

CREATE TABLE IF NOT EXISTS `chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `role` varchar(20) NOT NULL COMMENT 'user/assistant',
  `content` text NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

CREATE TABLE IF NOT EXISTS `knowledge_base` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question` varchar(255) NOT NULL,
  `answer` text NOT NULL,
  `category` varchar(50) DEFAULT NULL,
  `priority` int DEFAULT 0,
  `status` tinyint DEFAULT 1 COMMENT '1-启用, 0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 插入常见问题
INSERT INTO `knowledge_base` (`question`, `answer`, `category`, `priority`) VALUES
('如何租车', '您可以在首页浏览可租车辆，选择心仪的车型后点击预订，选择取车时间和门店即可完成预订。', '租车流程', 10),
('租车需要什么证件', '租车需要携带有效身份证和驾驶证，部分车型可能需要信用卡预授权。', '租车条件', 9),
('租车价格怎么算', '租车价格按天计算，不同车型价格不同。租金包含基础保险，押金在还车后退还。', '价格说明', 8),
('可以异地还车吗', '支持异地还车服务，但可能需要支付额外的异地还车费用，具体请咨询门店。', '还车服务', 7),
('取消订单会扣费吗', '取车前24小时取消订单可全额退款，24小时内取消可能收取一定手续费。', '订单取消', 6),
('车辆有保险吗', '所有租赁车辆都包含基础保险，您还可以选择购买额外的保险服务。', '保险服务', 5);

-- 评价表（如果不存在）
CREATE TABLE IF NOT EXISTS `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `car_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `rating` int NOT NULL COMMENT '评分1-5',
  `content` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_car_id` (`car_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';
