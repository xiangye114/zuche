-- 添加外键约束的SQL语句

SET FOREIGN_KEY_CHECKS = 0;

-- 1. car_images 表
ALTER TABLE `car_images`
ADD CONSTRAINT `fk_car_images_car_id` FOREIGN KEY (`car_id`) REFERENCES `cars`(`id`) ON DELETE CASCADE;

-- 2. car_similarity 表
ALTER TABLE `car_similarity`
ADD CONSTRAINT `fk_car_similarity_car_id_a` FOREIGN KEY (`car_id_a`) REFERENCES `cars`(`id`) ON DELETE CASCADE;

ALTER TABLE `car_similarity`
ADD CONSTRAINT `fk_car_similarity_car_id_b` FOREIGN KEY (`car_id_b`) REFERENCES `cars`(`id`) ON DELETE CASCADE;

-- 3. cars 表
ALTER TABLE `cars`
ADD CONSTRAINT `fk_cars_brand_id` FOREIGN KEY (`brand_id`) REFERENCES `brands`(`id`) ON DELETE SET NULL;

ALTER TABLE `cars`
ADD CONSTRAINT `fk_cars_category_id` FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`) ON DELETE SET NULL;

ALTER TABLE `cars`
ADD CONSTRAINT `fk_cars_store_id` FOREIGN KEY (`store_id`) REFERENCES `stores`(`id`) ON DELETE SET NULL;

-- 4. chat_messages 表
ALTER TABLE `chat_messages`
ADD CONSTRAINT `fk_chat_messages_session_id` FOREIGN KEY (`session_id`) REFERENCES `chat_sessions`(`id`) ON DELETE CASCADE;

-- 5. chat_sessions 表
ALTER TABLE `chat_sessions`
ADD CONSTRAINT `fk_chat_sessions_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE;

-- 6. favorites 表
ALTER TABLE `favorites`
ADD CONSTRAINT `fk_favorites_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE;

ALTER TABLE `favorites`
ADD CONSTRAINT `fk_favorites_car_id` FOREIGN KEY (`car_id`) REFERENCES `cars`(`id`) ON DELETE CASCADE;

-- 7. image_generation_logs 表
ALTER TABLE `image_generation_logs`
ADD CONSTRAINT `fk_image_generation_logs_car_id` FOREIGN KEY (`car_id`) REFERENCES `cars`(`id`) ON DELETE CASCADE;

-- 8. order_car_checks 表
ALTER TABLE `order_car_checks`
ADD CONSTRAINT `fk_order_car_checks_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE;

-- 9. order_logs 表
ALTER TABLE `order_logs`
ADD CONSTRAINT `fk_order_logs_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE;

ALTER TABLE `order_logs`
ADD CONSTRAINT `fk_order_logs_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `users`(`id`) ON DELETE SET NULL;

-- 10. orders 表
ALTER TABLE `orders`
ADD CONSTRAINT `fk_orders_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE;

ALTER TABLE `orders`
ADD CONSTRAINT `fk_orders_car_id` FOREIGN KEY (`car_id`) REFERENCES `cars`(`id`) ON DELETE RESTRICT;

ALTER TABLE `orders`
ADD CONSTRAINT `fk_orders_pickup_store_id` FOREIGN KEY (`pickup_store_id`) REFERENCES `stores`(`id`) ON DELETE SET NULL;

ALTER TABLE `orders`
ADD CONSTRAINT `fk_orders_return_store_id` FOREIGN KEY (`return_store_id`) REFERENCES `stores`(`id`) ON DELETE SET NULL;

-- 注意：cancel_reason_code 是 varchar 类型，需要确保 cancel_reasons 表的 reason_code 是唯一索引
ALTER TABLE `orders`
ADD CONSTRAINT `fk_orders_cancel_reason_code` FOREIGN KEY (`cancel_reason_code`) REFERENCES `cancel_reasons`(`reason_code`) ON DELETE SET NULL;

-- 11. reviews 表
ALTER TABLE `reviews`
ADD CONSTRAINT `fk_reviews_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE;

ALTER TABLE `reviews`
ADD CONSTRAINT `fk_reviews_car_id` FOREIGN KEY (`car_id`) REFERENCES `cars`(`id`) ON DELETE CASCADE;

ALTER TABLE `reviews`
ADD CONSTRAINT `fk_reviews_order_id` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE;

-- 12. stores 表
ALTER TABLE `stores`
ADD CONSTRAINT `fk_stores_admin_id` FOREIGN KEY (`admin_id`) REFERENCES `users`(`id`) ON DELETE SET NULL;

-- 13. user_behaviors 表
ALTER TABLE `user_behaviors`
ADD CONSTRAINT `fk_user_behaviors_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE;

ALTER TABLE `user_behaviors`
ADD CONSTRAINT `fk_user_behaviors_car_id` FOREIGN KEY (`car_id`) REFERENCES `cars`(`id`) ON DELETE CASCADE;

-- 14. user_profiles 表
ALTER TABLE `user_profiles`
ADD CONSTRAINT `fk_user_profiles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;