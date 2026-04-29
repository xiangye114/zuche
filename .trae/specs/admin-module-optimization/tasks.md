# 后台管理模块优化任务列表

## 任务概览

基于现有后台管理系统，按三阶段实施优化，严格控制新功能开发范围。

---

## 第一阶段：权限与表格优化（高优先级）

### 任务 1: 权限控制体系完善
**目标**: 基于users表role字段实现三级权限控制

#### 后端任务
- [ ] 1.1 创建UserRole枚举类
  - 定义USER(0)、SUPER_ADMIN(1)、STORE_ADMIN(2)
  - 添加角色描述和权限判断方法

- [ ] 1.2 修改User实体类
  - 添加role字段枚举类型支持
  - 添加角色判断辅助方法

- [ ] 1.3 实现数据权限过滤
  - StoreAdmin只能查询本门店数据
  - 在CarMapper查询中添加storeId过滤
  - 在OrderMapper查询中添加storeId过滤
  - 在UserMapper查询中添加角色过滤

- [ ] 1.4 接口权限控制
  - 在AdminController中添加角色校验
  - 门店管理接口仅限SUPER_ADMIN访问
  - 品牌/分类管理接口仅限SUPER_ADMIN修改

#### 前端任务
- [ ] 1.5 权限工具函数
  - 创建permission.js工具类
  - 实现角色判断函数
  - 实现权限校验函数

- [ ] 1.6 菜单权限控制
  - 根据role值动态显示/隐藏菜单项
  - StoreAdmin隐藏门店管理菜单
  - User隐藏后台管理入口

- [ ] 1.7 按钮权限控制
  - 封装v-permission指令
  - 根据role值控制操作按钮显示
  - StoreAdmin隐藏删除按钮

---

### 任务 2: 表格批量操作功能
**目标**: 为现有管理页面添加批量操作能力

#### 前端任务
- [ ] 2.1 封装批量操作表格组件
  - 基于现有el-table封装
  - 添加多选列
  - 添加批量操作按钮区域
  - 支持全选/反选

- [ ] 2.2 用户管理批量操作
  - 批量启用/禁用用户状态
  - 批量删除用户
  - 批量选择交互优化

- [ ] 2.3 车辆管理批量操作
  - 批量修改车辆状态
  - 批量删除车辆
  - 权限控制（StoreAdmin只能操作本门店车辆）

- [ ] 2.4 订单管理批量操作
  - 批量导出订单
  - 批量状态筛选

#### 后端任务
- [ ] 2.5 批量更新用户状态接口
  - POST `/admin/users/batch/status`
  - 参数：ids（用户ID数组）、status（目标状态）
  - 权限校验

- [ ] 2.6 批量更新车辆状态接口
  - POST `/admin/cars/batch/status`
  - 参数：ids（车辆ID数组）、status（目标状态）
  - 数据权限校验

- [ ] 2.7 批量删除接口
  - POST `/admin/users/batch/delete`
  - POST `/admin/cars/batch/delete`
  - 事务处理

---

### 任务 3: 数据导出功能
**目标**: 基于现有查询条件支持Excel导出

#### 后端任务
- [ ] 3.1 集成EasyExcel
  - 在pom.xml添加依赖
  - 创建ExcelExportUtil工具类

- [ ] 3.2 用户数据导出接口
  - GET `/admin/users/export`
  - 支持当前筛选条件
  - 数据权限控制

- [ ] 3.3 车辆数据导出接口
  - GET `/admin/cars/export`
  - 支持当前筛选条件

- [ ] 3.4 订单数据导出接口
  - GET `/admin/orders/export`
  - 支持状态筛选

#### 前端任务
- [ ] 3.5 导出按钮组件
  - 添加导出按钮到各管理页面
  - 显示导出进度
  - 文件下载处理

- [ ] 3.6 各页面集成
  - 用户管理页面导出
  - 车辆管理页面导出
  - 订单管理页面导出

---

## 第二阶段：体验优化（中优先级）

### 任务 4: 移动端适配优化
**目标**: 改善后台管理系统在移动端的用户体验

#### 前端任务
- [ ] 4.1 侧边栏移动端适配
  - <768px时改为抽屉式菜单
  - 添加遮罩层
  - 添加汉堡菜单按钮

- [ ] 4.2 表格移动端适配
  - 小屏幕下表格横向滚动
  - 操作按钮适配移动端

- [ ] 4.3 表单移动端适配
  - 移动端单列布局
  - 增大输入框触摸区域（不小于44px）

- [ ] 4.4 布局细节优化
  - 头部布局响应式调整
  - 内容区域padding优化

---

### 任务 5: 仪表盘数据可视化
**目标**: 基于现有Dashboard数据添加图表展示

#### 后端任务
- [ ] 5.1 扩展Dashboard接口
  - 添加近7天收入趋势数据
  - 添加订单状态分布数据
  - 添加用户增长趋势数据

#### 前端任务
- [ ] 5.2 集成ECharts
  - 安装vue-echarts
  - 封装图表组件

- [ ] 5.3 收入趋势图
  - 近7天收入曲线图
  - 放置于Dashboard页面

- [ ] 5.4 订单状态分布图
  - 饼图展示各状态占比

- [ ] 5.5 车辆状态统计图
  - 柱状图展示车辆状态分布

---

### 任务 6: 操作审计日志
**目标**: 使用AOP记录敏感操作

#### 后端任务
- [ ] 6.1 创建操作日志表
  ```sql
  CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '用户名',
    role INT COMMENT '角色',
    operation VARCHAR(50) COMMENT '操作类型',
    module VARCHAR(50) COMMENT '操作模块',
    description VARCHAR(500) COMMENT '操作描述',
    target_id BIGINT COMMENT '操作对象ID',
    old_data TEXT COMMENT '变更前数据',
    new_data TEXT COMMENT '变更后数据',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    status TINYINT COMMENT '状态 0-失败 1-成功',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_create_time (create_time)
  );
  ```

- [ ] 6.2 实现操作日志实体和Mapper
  - OperationLog.java
  - OperationLogMapper.java

- [ ] 6.3 实现AOP切面
  - @OperationLog注解
  - OperationLogAspect.java
  - 异步记录日志

- [ ] 6.4 添加操作日志注解
  - 用户状态变更方法
  - 车辆增删改方法
  - 订单状态变更方法

- [ ] 6.5 操作日志查询接口
  - GET `/admin/operation-logs`
  - 支持分页和筛选

#### 前端任务
- [ ] 6.6 操作日志页面
  - 日志列表展示
  - 时间范围筛选
  - 操作类型筛选

---

## 第三阶段：细节打磨（低优先级）

### 任务 7: 表单交互优化
**目标**: 改善现有表单的交互体验

#### 前端任务
- [ ] 7.1 表单验证优化
  - 统一验证提示样式
  - 实时验证反馈

- [ ] 7.2 防止重复提交
  - 提交按钮loading状态
  - 提交后禁用按钮

- [ ] 7.3 搜索功能优化
  - 搜索防抖处理
  - 记住搜索条件

---

## 任务依赖关系

```
第一阶段（可并行）:
├── 任务1: 权限控制体系
├── 任务2: 表格批量操作
└── 任务3: 数据导出

第二阶段（依赖第一阶段）:
├── 任务4: 移动端适配
├── 任务5: 仪表盘图表
└── 任务6: 操作审计日志

第三阶段（可选）:
└── 任务7: 表单交互优化
```

---

## 验收标准

### 功能验收
- [ ] 三级权限控制正常（role=0,1,2）
- [ ] StoreAdmin只能操作本门店数据
- [ ] 批量操作功能正常
- [ ] 数据导出功能正常
- [ ] 移动端布局正常
- [ ] 图表展示正确
- [ ] 操作日志记录完整

### 性能验收
- [ ] 页面加载时间 < 2s
- [ ] 批量操作响应 < 1s
- [ ] 数据导出不卡顿

### 兼容性
- [ ] Chrome/Firefox/Safari/Edge兼容
- [ ] 移动端浏览器兼容
