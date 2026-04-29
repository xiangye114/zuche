-- 为车辆分配门店，按顺序循环分配
UPDATE cars SET store_id = 1 WHERE id % 6 = 1;
UPDATE cars SET store_id = 2 WHERE id % 6 = 2;
UPDATE cars SET store_id = 3 WHERE id % 6 = 3;
UPDATE cars SET store_id = 4 WHERE id % 6 = 4;
UPDATE cars SET store_id = 5 WHERE id % 6 = 5;
UPDATE cars SET store_id = 6 WHERE id % 6 = 0;
