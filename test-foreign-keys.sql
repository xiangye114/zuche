-- 外键约束测试脚本

-- 1. 测试外键约束是否正常工作

-- 测试 1: 尝试插入无效的车辆图片（车辆ID不存在）
INSERT INTO `car_images` (`car_id`, `image_url`, `image_type`) VALUES (999999, 'test.jpg', 1);
-- 预期结果：外键约束错误，无法插入

-- 测试 2: 尝试删除有订单的车辆
-- 先查看是否有车辆有订单
SELECT c.id, c.name, COUNT(o.id) as order_count
FROM cars c
LEFT JOIN orders o ON c.id = o.car_id
WHERE o.id IS NOT NULL
LIMIT 1;

-- 尝试删除该车辆
DELETE FROM cars WHERE id = (SELECT c.id FROM cars c LEFT JOIN orders o ON c.id = o.car_id WHERE o.id IS NOT NULL LIMIT 1);
-- 预期结果：外键约束错误（ON DELETE RESTRICT），无法删除

-- 测试 3: 测试级联删除
-- 创建测试用户
INSERT INTO `users` (`username`, `password`, `phone`, `email`, `role`, `status`) VALUES ('test_user', 'password123', '13800138000', 'test@example.com', 0, 1);
SET @test_user_id = LAST_INSERT_ID();

-- 创建测试会话
INSERT INTO `chat_sessions` (`user_id`, `title`, `status`) VALUES (@test_user_id, 'Test Session', 1);
SET @test_session_id = LAST_INSERT_ID();

-- 创建测试消息
INSERT INTO `chat_messages` (`session_id`, `role`, `content`) VALUES (@test_session_id, 'user', 'Test message');

-- 验证消息存在
SELECT * FROM chat_messages WHERE session_id = @test_session_id;

-- 删除用户，测试级联删除
DELETE FROM users WHERE id = @test_user_id;

-- 验证会话和消息是否被级联删除
SELECT * FROM chat_sessions WHERE user_id = @test_user_id;
SELECT * FROM chat_messages WHERE session_id = @test_session_id;
-- 预期结果：会话和消息都被删除

-- 2. 性能测试

-- 测试 2.1: 插入性能测试

-- 记录开始时间
SET @start_time = NOW();

-- 插入100条测试数据
DELIMITER //
BEGIN
    DECLARE i INT DEFAULT 0;
    WHILE i < 100 DO
        INSERT INTO `favorites` (`user_id`, `car_id`) VALUES (1, FLOOR(RAND() * 40) + 1);
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 记录结束时间
SET @end_time = NOW();

-- 计算执行时间
SELECT TIMEDIFF(@end_time, @start_time) AS insert_time;

-- 清理测试数据
DELETE FROM favorites WHERE user_id = 1 AND car_id > 0;

-- 测试 2.2: 查询性能测试

-- 记录开始时间
SET @start_time = NOW();

-- 执行复杂查询
SELECT 
    o.id, o.order_no, u.username, c.name, s.name as store_name
FROM 
    orders o
JOIN 
    users u ON o.user_id = u.id
JOIN 
    cars c ON o.car_id = c.id
LEFT JOIN 
    stores s ON o.pickup_store_id = s.id
WHERE 
    o.status = 1
ORDER BY 
    o.create_time DESC
LIMIT 10;

-- 记录结束时间
SET @end_time = NOW();

-- 计算执行时间
SELECT TIMEDIFF(@end_time, @start_time) AS query_time;

-- 3. 验证外键约束是否全部生效

-- 检查所有外键约束
SELECT 
    table_name, 
    constraint_name, 
    referenced_table_name, 
    referenced_column_name
FROM 
    information_schema.key_column_usage
WHERE 
    table_schema = 'zuche' 
    AND referenced_table_name IS NOT NULL
ORDER BY 
    table_name, constraint_name;