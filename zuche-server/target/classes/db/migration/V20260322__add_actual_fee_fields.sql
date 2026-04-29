-- ========================================================-- 添加实际费用字段 - 数据库变更脚本-- 创建时间: 2026-03-22-- 说明: 为支持基于实际使用时间的费用计算，添加实际费用相关字段-- ========================================================-- 添加实际费用字段到orders表
ALTER TABLE orders ADD COLUMN actual_rental_days INT DEFAULT NULL COMMENT '实际租车天数' AFTER settlement_by,
ADD COLUMN actual_rental_fee DECIMAL(10,2) DEFAULT NULL COMMENT '实际租金费用' AFTER actual_rental_days,
ADD COLUMN actual_insurance_fee DECIMAL(10,2) DEFAULT NULL COMMENT '实际保险费用' AFTER actual_rental_fee,
ADD COLUMN actual_service_fee DECIMAL(10,2) DEFAULT NULL COMMENT '实际服务费用' AFTER actual_insurance_fee,
ADD COLUMN actual_overtime_fee DECIMAL(10,2) DEFAULT NULL COMMENT '实际超时费用' AFTER actual_service_fee;-- 添加索引
CREATE INDEX idx_actual_rental_days ON orders(actual_rental_days);-- 数据迁移说明：-- 1. 历史订单的actual_rental_fee等字段保持为NULL-- 2. 新订单在结算时会自动计算并填充这些字段-- 3. 对于已完成的订单，可以通过脚本批量计算填充（可选）
