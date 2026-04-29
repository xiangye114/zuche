-- 创建数据库
CREATE DATABASE IF NOT EXISTS zuche DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zuche;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像URL',
    role TINYINT DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 品牌表
CREATE TABLE IF NOT EXISTS brands (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '品牌名称',
    logo VARCHAR(255) COMMENT '品牌Logo',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

-- 分类表
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 门店表
CREATE TABLE IF NOT EXISTS stores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '门店名称',
    address VARCHAR(255) COMMENT '门店地址',
    phone VARCHAR(20) COMMENT '联系电话',
    latitude DECIMAL(10,6) COMMENT '纬度',
    longitude DECIMAL(10,6) COMMENT '经度',
    admin_id BIGINT COMMENT '门店管理员ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0-关闭，1-营业中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店表';

-- 车辆表
CREATE TABLE IF NOT EXISTS cars (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '车辆名称',
    brand_id BIGINT COMMENT '品牌ID',
    category_id BIGINT COMMENT '分类ID',
    store_id BIGINT COMMENT '所属门店ID',
    color VARCHAR(20) COMMENT '颜色',
    price_per_day DECIMAL(10,2) NOT NULL COMMENT '日租金',
    deposit DECIMAL(10,2) COMMENT '押金',
    seats INT COMMENT '座位数',
    transmission VARCHAR(20) COMMENT '变速箱类型',
    fuel_type VARCHAR(20) COMMENT '燃料类型',
    image VARCHAR(255) COMMENT '车辆主图',
    description TEXT COMMENT '车辆描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-维修中，1-可租，2-已租',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_brand_id (brand_id),
    KEY idx_category_id (category_id),
    KEY idx_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- 车辆图片表
CREATE TABLE IF NOT EXISTS car_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    car_id BIGINT NOT NULL COMMENT '车辆ID',
    image_url VARCHAR(255) NOT NULL COMMENT '图片URL',
    image_type TINYINT DEFAULT 1 COMMENT '图片类型：1-AI生成，2-手动上传',
    prompt VARCHAR(500) COMMENT 'AI生成提示词',
    angle VARCHAR(50) COMMENT '角度：front/side/rear/interior',
    is_main TINYINT DEFAULT 0 COMMENT '是否主图：0-否，1-是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_car_id (car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆图片表';

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    car_id BIGINT NOT NULL COMMENT '车辆ID',
    start_time DATETIME NOT NULL COMMENT '计划取车时间',
    end_time DATETIME NOT NULL COMMENT '计划还车时间',
    total_price DECIMAL(10,2) COMMENT '总金额',
    deposit DECIMAL(10,2) COMMENT '押金',
    pickup_store_id BIGINT COMMENT '取车门店ID',
    return_store_id BIGINT COMMENT '还车门店ID',
    pickup_time DATETIME COMMENT '实际取车时间',
    pickup_operator VARCHAR(50) COMMENT '取车操作人',
    pickup_remark VARCHAR(500) COMMENT '取车备注',
    return_time DATETIME COMMENT '实际还车时间',
    return_operator VARCHAR(50) COMMENT '还车操作人',
    return_remark VARCHAR(500) COMMENT '还车备注',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待确认，1-已确认，2-已取车，3-已还车，4-已完成，5-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_user_id (user_id),
    KEY idx_car_id (car_id),
    KEY idx_order_no (order_no),
    KEY idx_pickup_store_id (pickup_store_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单状态变更日志表
CREATE TABLE IF NOT EXISTS order_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人名称',
    from_status INT COMMENT '变更前状态',
    to_status INT COMMENT '变更后状态',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_order_id (order_id),
    KEY idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态变更日志表';

-- 评价表
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    car_id BIGINT NOT NULL COMMENT '车辆ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    rating INT NOT NULL COMMENT '评分（1-5）',
    content TEXT COMMENT '评价内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_user_id (user_id),
    KEY idx_car_id (car_id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 收藏表
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    car_id BIGINT NOT NULL COMMENT '车辆ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_user_car (user_id, car_id),
    KEY idx_user_id (user_id),
    KEY idx_car_id (car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 聊天会话表
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    title VARCHAR(100) COMMENT '会话标题',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已结束，1-进行中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色：user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 知识库表
CREATE TABLE IF NOT EXISTS knowledge_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    question VARCHAR(255) NOT NULL COMMENT '问题关键词',
    answer TEXT NOT NULL COMMENT '答案内容',
    category VARCHAR(50) COMMENT '分类',
    priority INT DEFAULT 0 COMMENT '优先级',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 用户行为记录表
CREATE TABLE IF NOT EXISTS user_behaviors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    car_id BIGINT NOT NULL COMMENT '车辆ID',
    behavior_type TINYINT NOT NULL COMMENT '行为类型：1-浏览，2-收藏，3-下单，4-评价',
    score DECIMAL(3,1) DEFAULT 1.0 COMMENT '行为得分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_car_id (car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为记录表';

-- 车辆相似度表
CREATE TABLE IF NOT EXISTS car_similarity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    car_id_a BIGINT NOT NULL COMMENT '车辆A的ID',
    car_id_b BIGINT NOT NULL COMMENT '车辆B的ID',
    similarity DECIMAL(5,4) NOT NULL COMMENT '相似度（0-1之间）',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_car_pair (car_id_a, car_id_b),
    KEY idx_car_id_a (car_id_a),
    KEY idx_car_id_b (car_id_b)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆相似度表';

-- 用户画像表
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    preferred_brand VARCHAR(50) COMMENT '偏好品牌',
    preferred_category VARCHAR(50) COMMENT '偏好车型',
    preferred_price_range VARCHAR(20) COMMENT '偏好价格区间',
    preferred_seats INT COMMENT '偏好座位数',
    avg_rent_days DECIMAL(4,1) COMMENT '平均租车天数',
    total_orders INT DEFAULT 0 COMMENT '总订单数',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户画像表';

-- 图片生成记录表
CREATE TABLE IF NOT EXISTS image_generation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    car_id BIGINT NOT NULL COMMENT '车辆ID',
    prompt VARCHAR(500) COMMENT '生成提示词',
    result_url VARCHAR(255) COMMENT '生成的图片URL',
    status TINYINT DEFAULT 0 COMMENT '状态：0-生成中，1-成功，2-失败',
    error_msg VARCHAR(255) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_car_id (car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片生成记录表';

-- 插入初始管理员账号（密码：admin123，使用BCrypt加密）
INSERT INTO users (username, password, role, status) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1, 1);

-- 插入初始品牌数据
INSERT INTO brands (name, logo) VALUES 
('宝马', '/images/brands/bmw.png'),
('奔驰', '/images/brands/benz.png'),
('奥迪', '/images/brands/audi.png'),
('大众', '/images/brands/volkswagen.png'),
('丰田', '/images/brands/toyota.png'),
('本田', '/images/brands/honda.png'),
('特斯拉', '/images/brands/tesla.png'),
('保时捷', '/images/brands/porsche.png');

-- 插入初始分类数据
INSERT INTO categories (name, description) VALUES 
('经济型', '经济实惠，适合日常代步'),
('舒适型', '舒适宽敞，适合家庭出行'),
('SUV', '空间大，适合自驾游'),
('商务车', '高端大气，适合商务接待'),
('豪华车', '尊贵体验，适合特殊场合'),
('新能源', '环保节能，绿色出行');

-- 插入初始门店数据
INSERT INTO stores (name, address, phone, status) VALUES 
('机场店', '机场T2航站楼到达层', '400-123-0001', 1),
('火车站店', '火车站东广场', '400-123-0002', 1),
('市中心店', '市中心商业街88号', '400-123-0003', 1),
('高新区店', '高新区科技路100号', '400-123-0004', 1);

-- 插入初始知识库数据
INSERT INTO knowledge_base (question, answer, category, priority) VALUES 
('如何租车', '租车流程非常简单：1.注册登录账号 2.浏览选择心仪车辆 3.选择租期和取还车门店 4.确认订单信息并提交即可。', '租车流程', 10),
('需要什么证件', '租车需要提供：1.有效身份证件 2.有效驾驶证（驾龄需满1年以上）3.信用卡或押金用于担保。', '租车流程', 9),
('车辆类型', '我们提供多种车型：经济型、舒适型、SUV、商务车、豪华车、新能源车等，满足不同出行需求。', '车辆信息', 8),
('如何取消订单', '取消订单规则：取车前24小时以上取消，全额退款；取车前2-24小时取消，扣除租金的30%；取车前2小时内取消，扣除租金的50%。', '订单问题', 9),
('订单状态查询', '您可以在"个人中心-我的订单"中查看所有订单状态，包括待确认、已确认、进行中、已完成等状态。', '订单问题', 8),
('租车费用包含', '租车费用包含：日租金、基础保险费、服务费。不包含：油费、过路费、额外保险、超时费等。', '费用说明', 7),
('营业时间', '各门店营业时间略有不同，一般为8:00-20:00，具体请查看门店详情页或致电咨询。', '门店服务', 6),
('还车流程', '还车时请将车辆开至指定门店，工作人员会检查车辆状况，确认无误后退还押金。请确保油量与取车时一致。', '租车流程', 8);
