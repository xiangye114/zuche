# 租车后台订单状态管理流程优化计划

## 一、项目概述

基于现有租车管理系统，优化订单状态管理流程，实现完整的6状态订单生命周期管理，打通管理员后台与用户中心的订单流转。

## 二、现有状态分析

### 2.1 当前状态定义
- 0: 待确认 - 订单创建后等待处理
- 1: 已确认 - 等待取车
- 2: 已取车 - 用车中
- 3: 已还车 - 待结算
- 4: 已完成 - 订单完成
- 5: 已取消 - 订单已取消

### 2.2 当前问题
1. 缺少车辆可用性检查机制
2. 缺少用户资质审核功能
3. 缺少车辆检查记录功能
4. 缺少还车结算功能
5. 缺少取消原因分类统计
6. 状态流转权限控制不够细致

## 三、优化目标

### 3.1 核心目标
1. 完善6个订单状态的业务功能
2. 实现状态流转的权限控制
3. 增加车辆可用性检查
4. 增加用户资质审核
5. 实现完整的日志记录
6. 提供状态流程可视化

### 3.2 技术目标
1. 保持与现有架构兼容
2. 不破坏现有数据
3. 前后端完整打通
4. 支持数据统计分析

## 四、详细实施步骤

### 阶段一：数据库结构优化

#### 步骤1.1：扩展orders表字段
```sql
-- 新增字段
ALTER TABLE orders ADD COLUMN rental_days INT COMMENT '租车天数' AFTER end_time;
ALTER TABLE orders ADD COLUMN daily_rate DECIMAL(10,2) COMMENT '日租金' AFTER rental_days;
ALTER TABLE orders ADD COLUMN rental_fee DECIMAL(10,2) COMMENT '租金费用' AFTER daily_rate;
ALTER TABLE orders ADD COLUMN insurance_fee DECIMAL(10,2) COMMENT '保险费用' AFTER rental_fee;
ALTER TABLE orders ADD COLUMN service_fee DECIMAL(10,2) COMMENT '服务费用' AFTER insurance_fee;
ALTER TABLE orders ADD COLUMN overtime_fee DECIMAL(10,2) COMMENT '超时费用' AFTER service_fee;
ALTER TABLE orders ADD COLUMN mileage_fee DECIMAL(10,2) COMMENT '超里程费用' AFTER overtime_fee;
ALTER TABLE orders ADD COLUMN discount_amount DECIMAL(10,2) COMMENT '优惠金额' AFTER mileage_fee;
ALTER TABLE orders ADD COLUMN pay_time DATETIME COMMENT '支付时间' AFTER update_time;
ALTER TABLE orders ADD COLUMN confirm_time DATETIME COMMENT '确认时间' AFTER pay_time;
ALTER TABLE orders ADD COLUMN cancel_time DATETIME COMMENT '取消时间' AFTER confirm_time;
ALTER TABLE orders ADD COLUMN cancel_reason VARCHAR(500) COMMENT '取消原因' AFTER cancel_time;
ALTER TABLE orders ADD COLUMN cancel_by VARCHAR(50) COMMENT '取消人' AFTER cancel_reason;
ALTER TABLE orders ADD COLUMN mileage_out INT COMMENT '取车里程' AFTER return_remark;
ALTER TABLE orders ADD COLUMN mileage_in INT COMMENT '还车里程' AFTER mileage_out;
ALTER TABLE orders ADD COLUMN fuel_level_out TINYINT COMMENT '取车油量(百分比)' AFTER mileage_in;
ALTER TABLE orders ADD COLUMN fuel_level_in TINYINT COMMENT '还车油量(百分比)' AFTER fuel_level_out;
ALTER TABLE orders ADD COLUMN damage_description VARCHAR(1000) COMMENT '车辆损伤描述' AFTER fuel_level_in;
ALTER TABLE orders ADD COLUMN damage_photos TEXT COMMENT '损伤照片JSON数组' AFTER damage_description;
ALTER TABLE orders ADD COLUMN settlement_amount DECIMAL(10,2) COMMENT '结算金额' AFTER damage_photos;
ALTER TABLE orders ADD COLUMN settlement_time DATETIME COMMENT '结算时间' AFTER settlement_amount;
ALTER TABLE orders ADD COLUMN settlement_by VARCHAR(50) COMMENT '结算人' AFTER settlement_time;
```

#### 步骤1.2：创建车辆检查记录表
```sql
CREATE TABLE order_car_checks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    check_type TINYINT NOT NULL COMMENT '检查类型：1-取车检查，2-还车检查',
    check_item VARCHAR(100) NOT NULL COMMENT '检查项目',
    check_result TINYINT NOT NULL COMMENT '检查结果：0-异常，1-正常',
    description VARCHAR(500) COMMENT '问题描述',
    photos TEXT COMMENT '照片JSON数组',
    checked_by VARCHAR(50) COMMENT '检查人',
    checked_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '检查时间',
    remark VARCHAR(500) COMMENT '备注',
    KEY idx_order_id (order_id),
    KEY idx_check_type (check_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单车辆检查记录表';
```

#### 步骤1.3：创建取消原因分类表
```sql
CREATE TABLE cancel_reasons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    reason_code VARCHAR(50) NOT NULL COMMENT '原因编码',
    reason_name VARCHAR(100) NOT NULL COMMENT '原因名称',
    reason_type TINYINT NOT NULL COMMENT '类型：1-用户取消，2-系统取消，3-管理员取消',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='取消原因分类表';

-- 初始化数据
INSERT INTO cancel_reasons (reason_code, reason_name, reason_type, sort_order) VALUES
('USER_CHANGE_PLAN', '用户改变行程计划', 1, 1),
('USER_FIND_BETTER', '用户找到更优惠的价格', 1, 2),
('USER_NO_LICENSE', '用户无法提供有效驾照', 1, 3),
('USER_OTHER', '用户其他原因', 1, 99),
('SYS_NO_PAY', '超时未支付', 2, 1),
('SYS_CAR_UNAVAILABLE', '车辆不可用', 2, 2),
('ADMIN_FRAUD', '发现欺诈行为', 3, 1),
('ADMIN_VIOLATION', '违反租赁条款', 3, 2),
('ADMIN_OTHER', '管理员其他原因', 3, 99);
```

### 阶段二：后端实体类扩展

#### 步骤2.1：扩展Order实体类
文件：zuche-server/src/main/java/com/zuche/entity/Order.java

新增字段：
- rentalDays, dailyRate, rentalFee, insuranceFee, serviceFee
- overtimeFee, mileageFee, discountAmount, payTime
- confirmTime, cancelTime, cancelReason, cancelBy
- mileageOut, mileageIn, fuelLevelOut, fuelLevelIn
- damageDescription, damagePhotos, settlementAmount
- settlementTime, settlementBy

#### 步骤2.2：创建OrderCarCheck实体类
文件：zuche-server/src/main/java/com/zuche/entity/OrderCarCheck.java

字段：
- id, orderId, checkType, checkItem, checkResult
- description, photos, checkedBy, checkedTime, remark

#### 步骤2.3：创建CancelReason实体类
文件：zuche-server/src/main/java/com/zuche/entity/CancelReason.java

字段：
- id, reasonCode, reasonName, reasonType, sortOrder, isActive, createTime

### 阶段三：DTO扩展

#### 步骤3.1：扩展OrderDetailDTO
文件：zuche-server/src/main/java/com/zuche/dto/OrderDetailDTO.java

新增字段：
- 费用明细字段
- 时间记录字段
- 车辆检查记录列表
- 取消原因信息
- 结算信息

#### 步骤3.2：创建OrderCarCheckDTO
文件：zuche-server/src/main/java/com/zuche/dto/OrderCarCheckDTO.java

#### 步骤3.3：创建CancelReasonDTO
文件：zuche-server/src/main/java/com/zuche/dto/CancelReasonDTO.java

#### 步骤3.4：扩展OrderQueryDTO
新增查询条件：
- 取消原因筛选
- 结算状态筛选
- 车辆检查状态筛选

### 阶段四：Service层扩展

#### 步骤4.1：扩展OrderService接口
文件：zuche-server/src/main/java/com/zuche/service/OrderService.java

新增方法：
```java
// 待确认状态操作
void confirmOrder(Long orderId, Long operatorId, String operatorName, String remark);
void rejectOrder(Long orderId, Long operatorId, String operatorName, String reasonCode, String remark);
boolean checkCarAvailability(Long carId, LocalDateTime startTime, LocalDateTime endTime);
boolean checkUserQualification(Long userId);

// 已确认状态操作
void prepareCar(Long orderId, List<OrderCarCheckDTO> checkItems);
List<OrderCarCheckDTO> getCarChecks(Long orderId, Integer checkType);

// 已取车状态操作
void reportCarIssue(Long orderId, String issueType, String description, String photos);

// 已还车状态操作
void checkReturnCar(Long orderId, List<OrderCarCheckDTO> checkItems);
void settleOrder(Long orderId, Double settlementAmount, Long operatorId, String operatorName);

// 已取消状态操作
void cancelOrderWithReason(Long orderId, String reasonCode, String reasonDetail, Long operatorId, String operatorName);
List<CancelReasonDTO> getCancelReasons(Integer reasonType);

// 统计功能
Map<String, Object> getOrderStatistics(String timeRange);
Map<String, Object> getCancelStatistics(String timeRange);
```

#### 步骤4.2：实现OrderService新方法
文件：zuche-server/src/main/java/com/zuche/service/impl/OrderServiceImpl.java

实现上述所有新方法，确保：
1. 状态校验
2. 权限检查
3. 日志记录
4. 事务控制

### 阶段五：Controller层扩展

#### 步骤5.1：扩展AdminOrderController
文件：zuche-server/src/main/java/com/zuche/controller/admin/AdminOrderController.java

新增API端点：
```java
// 待确认状态
@PutMapping("/{id}/confirm") - 确认订单
@PutMapping("/{id}/reject") - 拒绝订单
@GetMapping("/check-car-availability") - 检查车辆可用性
@GetMapping("/check-user-qualification") - 检查用户资质

// 已确认状态
@PostMapping("/{id}/car-checks") - 提交车辆检查
@GetMapping("/{id}/car-checks") - 获取车辆检查记录

// 已取车状态
@PostMapping("/{id}/report-issue") - 上报车辆问题

// 已还车状态
@PutMapping("/{id}/settle") - 结算订单

// 已取消状态
@PutMapping("/{id}/cancel") - 取消订单（带原因）
@GetMapping("/cancel-reasons") - 获取取消原因列表

// 统计功能
@GetMapping("/statistics/cancel") - 取消统计
```

### 阶段六：前端页面优化

#### 步骤6.1：优化OrderManage.vue
文件：zuche-web/src/views/admin/OrderManage.vue

新增功能：
1. **待确认状态操作区**：
   - 确认订单按钮（带确认对话框）
   - 拒绝订单按钮（带原因选择）
   - 车辆可用性检查显示
   - 用户资质审核显示

2. **已确认状态操作区**：
   - 车辆准备清单
   - 车辆检查表单
   - 预计取车时间提醒

3. **已取车状态操作区**：
   - 车辆位置显示（如有GPS）
   - 异常上报按钮
   - 还车提醒设置

4. **已还车状态操作区**：
   - 还车检查表单
   - 损伤对比显示
   - 结算金额计算
   - 结算确认按钮

5. **已取消状态显示**：
   - 取消原因显示
   - 取消时间/人显示
   - 退款状态显示

6. **状态流程图**：
   - 使用Steps组件展示当前状态
   - 高亮当前步骤
   - 显示可执行操作

#### 步骤6.2：优化订单详情对话框
新增卡片：
- 车辆检查记录卡片
- 费用明细卡片（优化）
- 取消信息卡片
- 结算信息卡片

#### 步骤6.3：新增组件
1. **CarCheckForm.vue** - 车辆检查表单组件
2. **CancelReasonSelect.vue** - 取消原因选择组件
3. **SettlementDialog.vue** - 结算对话框组件
4. **OrderStatusFlow.vue** - 订单状态流程图组件

### 阶段七：前端API扩展

#### 步骤7.1：扩展admin.js
文件：zuche-web/src/api/admin.js

新增API：
```javascript
// 待确认状态
confirmOrder: (id, data) => request.put(`/admin/orders/${id}/confirm`, data)
rejectOrder: (id, data) => request.put(`/admin/orders/${id}/reject`, data)
checkCarAvailability: (params) => request.get('/admin/orders/check-car-availability', { params })
checkUserQualification: (userId) => request.get(`/admin/orders/check-user-qualification?userId=${userId}`)

// 已确认状态
submitCarChecks: (id, data) => request.post(`/admin/orders/${id}/car-checks`, data)
getCarChecks: (id, type) => request.get(`/admin/orders/${id}/car-checks?type=${type}`)

// 已取车状态
reportCarIssue: (id, data) => request.post(`/admin/orders/${id}/report-issue`, data)

// 已还车状态
settleOrder: (id, data) => request.put(`/admin/orders/${id}/settle`, data)

// 已取消状态
cancelOrder: (id, data) => request.put(`/admin/orders/${id}/cancel`, data)
getCancelReasons: (type) => request.get(`/admin/orders/cancel-reasons?type=${type}`)

// 统计
getCancelStatistics: (params) => request.get('/admin/orders/statistics/cancel', { params })
```

### 阶段八：用户中心订单页面优化

#### 步骤8.1：优化Orders.vue
文件：zuche-web/src/views/Orders.vue

新增功能：
1. 状态流程图展示
2. 取消原因查看
3. 车辆检查记录查看
4. 结算信息查看

#### 步骤8.2：优化OrderDetail.vue
文件：zuche-web/src/views/OrderDetail.vue

新增功能：
1. 完整状态时间线
2. 车辆检查记录
3. 费用明细优化
4. 取消/结算信息

### 阶段九：数据迁移与初始化

#### 步骤9.1：数据迁移脚本
```sql
-- 更新现有订单数据
UPDATE orders SET rental_days = DATEDIFF(end_time, start_time) WHERE rental_days IS NULL;
UPDATE orders SET rental_fee = total_price WHERE rental_fee IS NULL;
```

#### 步骤9.2：初始化取消原因数据
执行步骤1.3中的INSERT语句

### 阶段十：测试验证

#### 步骤10.1：单元测试
- OrderService新方法测试
- 状态流转测试
- 权限控制测试

#### 步骤10.2：集成测试
- 完整订单流程测试
- 前后端联调测试
- 数据统计测试

#### 步骤10.3：回归测试
- 现有功能不受影响
- 数据一致性检查

### 阶段十一：需求文档更新

#### 步骤11.1：更新需求规格说明书
文件：documents/requirements.md 或创建新的需求文档

更新内容：
1. **功能需求章节**
   - 新增6个订单状态的详细功能描述
   - 每个状态的操作流程和权限控制
   - 车辆可用性检查机制
   - 用户资质审核流程
   - 车辆检查记录功能
   - 结算功能描述

2. **接口需求章节**
   - 新增API接口列表
   - 请求/响应参数说明
   - 错误码定义

3. **数据需求章节**
   - 数据库表结构变更
   - 新增表说明
   - 字段定义

4. **非功能需求章节**
   - 性能要求
   - 安全要求
   - 兼容性要求

#### 步骤11.2：更新接口文档
文件：documents/api.md 或使用Swagger注解

更新内容：
1. 新增API端点说明
2. 请求参数示例
3. 响应数据示例
4. 错误处理说明

#### 步骤11.3：更新数据库文档
文件：documents/database.md

更新内容：
1. ER图更新
2. 表结构变更说明
3. 索引设计说明
4. 数据迁移脚本

#### 步骤11.4：更新操作手册
文件：documents/operation-manual.md

更新内容：
1. 订单状态流转操作指南
2. 各状态操作步骤
3. 常见问题处理
4. 权限配置说明

## 五、状态流转图

```
                    ┌─────────────┐
                    │   待确认    │
                    │   (0)       │
                    └──────┬──────┘
                           │
           ┌───────────────┼───────────────┐
           │               │               │
           ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │  确认通过   │ │  确认不通过 │ │   取消      │
    │  (1)已确认  │ │  (5)已取消  │ │  (5)已取消  │
    └──────┬──────┘ └─────────────┘ └─────────────┘
           │
           ▼
    ┌─────────────┐
    │   已确认    │
    │   (1)       │
    └──────┬──────┘
           │
           ▼
    ┌─────────────┐
    │   已取车    │
    │   (2)       │
    └──────┬──────┘
           │
           ▼
    ┌─────────────┐
    │   已还车    │
    │   (3)       │
    └──────┬──────┘
           │
           ▼
    ┌─────────────┐
    │   已完成    │
    │   (4)       │
    └─────────────┘
```

## 六、权限矩阵

| 操作 | 管理员 | 门店管理员 | 客服 | 财务 |
|------|--------|------------|------|------|
| 确认订单 | ✅ | ✅ | ✅ | ❌ |
| 拒绝订单 | ✅ | ✅ | ✅ | ❌ |
| 确认取车 | ✅ | ✅ | ✅ | ❌ |
| 确认还车 | ✅ | ✅ | ✅ | ❌ |
| 结算订单 | ✅ | ❌ | ❌ | ✅ |
| 取消订单 | ✅ | ✅ | ✅ | ❌ |
| 查看统计 | ✅ | ✅(本门店) | ❌ | ✅ |

## 七、风险评估

### 7.1 技术风险
- **数据库字段扩展**：需要确保新字段有默认值，不影响现有数据
- **状态流转兼容性**：新状态流转逻辑需要与现有逻辑兼容

### 7.2 缓解措施
- 所有新字段都设置默认值或允许NULL
- 保留现有API不变，新增API扩展功能
- 充分测试后再上线

## 八、实施时间表

| 阶段 | 预计时间 | 依赖 |
|------|----------|------|
| 阶段一：数据库 | 2小时 | 无 |
| 阶段二：实体类 | 2小时 | 阶段一 |
| 阶段三：DTO | 1小时 | 阶段二 |
| 阶段四：Service | 4小时 | 阶段三 |
| 阶段五：Controller | 3小时 | 阶段四 |
| 阶段六：前端页面 | 6小时 | 阶段五 |
| 阶段七：API | 1小时 | 阶段六 |
| 阶段八：用户中心 | 3小时 | 阶段七 |
| 阶段九：数据迁移 | 1小时 | 阶段八 |
| 阶段十：测试 | 4小时 | 阶段九 |
| 阶段十一：需求文档更新 | 3小时 | 阶段十 |
| **总计** | **30小时** | - |

## 九、验收标准

1. ✅ 6个订单状态功能完整
2. ✅ 状态流转权限控制正确
3. ✅ 车辆可用性检查正常
4. ✅ 用户资质审核正常
5. ✅ 日志记录完整
6. ✅ 状态流程可视化正常
7. ✅ 前后端数据一致
8. ✅ 现有功能不受影响
