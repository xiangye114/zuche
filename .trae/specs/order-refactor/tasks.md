# 租车系统 - 订单逻辑重构实施计划

## [x] Task 1: 后端订单状态管理优化
- **Priority**: P0
- **Depends On**: None
- **Description**: 
  - 更新OrderServiceImpl.java中的状态管理逻辑
  - 实现未付款订单的30分钟自动取消功能
  - 添加门店管理员确认用车和还车的方法
- **Acceptance Criteria Addressed**: AC-2, AC-3, AC-4, AC-5
- **Test Requirements**:
  - `programmatic` TR-1.1: 订单创建后状态为未付款
  - `programmatic` TR-1.2: 支付成功后状态变更为待取车
  - `programmatic` TR-1.3: 门店管理员确认用车后状态变更为用车中
  - `programmatic` TR-1.4: 门店管理员确认还车后状态变更为已还车
  - `programmatic` TR-1.5: 30分钟未付款订单自动取消
- **Notes**: 需要修改OrderServiceImpl.java和OrderCancelTask.java

## [x] Task 2: 前端支付确认弹窗实现
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 在CarDetail.vue中添加支付确认弹窗
  - 实现30分钟倒计时功能
  - 集成支付确认逻辑
- **Acceptance Criteria Addressed**: AC-1, AC-3
- **Test Requirements**:
  - `human-judgment` TR-2.1: 点击预定按钮后弹出支付确认弹窗
  - `human-judgment` TR-2.2: 弹窗显示订单详情和30分钟倒计时
  - `programmatic` TR-2.3: 点击确认支付后订单状态变更为待取车
- **Notes**: 需要修改CarDetail.vue文件

## [x] Task 3: 前端订单页面页签更新
- **Priority**: P0
- **Depends On**: Task 1
- **Description**: 
  - 更新Orders.vue中的页签显示，只保留全部、待取车、用车中、已还车四个页签
  - 更新状态显示和过滤逻辑
- **Acceptance Criteria Addressed**: AC-6
- **Test Requirements**:
  - `human-judgment` TR-3.1: 订单页面只显示四个指定页签
  - `programmatic` TR-3.2: 页签过滤功能正常工作
- **Notes**: 需要修改Orders.vue文件

## [x] Task 4: 门店管理员操作界面优化
- **Priority**: P1
- **Depends On**: Task 1
- **Description**: 
  - 添加门店管理员确认用车和还车的操作按钮
  - 实现确认操作的API调用
- **Acceptance Criteria Addressed**: AC-4, AC-5
- **Test Requirements**:
  - `human-judgment` TR-4.1: 门店管理员可以看到确认用车和还车按钮
  - `programmatic` TR-4.2: 点击确认按钮后订单状态正确变更
- **Notes**: 需要在订单管理页面添加相应操作

## [x] Task 5: 系统测试和验证
- **Priority**: P1
- **Depends On**: Task 1, Task 2, Task 3, Task 4
- **Description**: 
  - 测试完整的订单流程
  - 验证30分钟自动取消功能
  - 测试门店管理员操作
- **Acceptance Criteria Addressed**: AC-1, AC-2, AC-3, AC-4, AC-5, AC-6
- **Test Requirements**:
  - `programmatic` TR-5.1: 完整订单流程测试通过
  - `programmatic` TR-5.2: 30分钟自动取消功能测试通过
  - `human-judgment` TR-5.3: 所有功能正常运行
- **Notes**: 需要运行完整的测试流程