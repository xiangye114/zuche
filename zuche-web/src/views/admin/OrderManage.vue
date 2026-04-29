<template>
  <div class="admin-orders-page">
    <div class="page-header">
      <h1>订单管理</h1>
      <p>管理所有租车订单，处理取车和还车确认</p>
    </div>

    <div class="orders-content">
      <!-- 高级搜索栏 -->
      <div class="search-bar">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-input
              v-model="searchForm.orderNo"
              placeholder="订单号"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Document /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-input
              v-model="searchForm.userName"
              placeholder="用户姓名"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-input
              v-model="searchForm.userPhone"
              placeholder="手机号"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-select v-model="searchForm.status" placeholder="订单状态" clearable style="width: 100%">
              <el-option label="全部状态" value="" />
              <el-option label="待确认" :value="0" />
              <el-option label="已确认" :value="1" />
              <el-option label="已取车" :value="2" />
              <el-option label="已还车" :value="3" />
              <el-option label="已完成" :value="4" />
              <el-option label="已取消" :value="5" />
            </el-select>
          </el-col>
        </el-row>
        
        <el-row :gutter="16" style="margin-top: 12px;">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-select v-model="searchForm.timeRange" placeholder="时间范围" clearable style="width: 100%">
              <el-option label="全部时间" value="" />
              <el-option label="今日" value="today" />
              <el-option label="本周" value="week" />
              <el-option label="本月" value="month" />
              <el-option label="自定义" value="custom" />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-if="searchForm.timeRange === 'custom'">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 100%"
              @change="handleDateRangeChange"
            />
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-select v-model="searchForm.storeId" placeholder="门店" clearable style="width: 100%">
              <el-option label="全部门店" value="" />
              <el-option 
                v-for="store in storeList" 
                :key="store.id" 
                :label="store.name" 
                :value="store.id" 
              />
            </el-select>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 状态筛选标签 -->
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" @change="handleFilterChange" size="large">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="0">
            <el-badge :value="statusCount[0]" :hidden="!statusCount[0]">待确认</el-badge>
          </el-radio-button>
          <el-radio-button label="1">
            <el-badge :value="statusCount[1]" :hidden="!statusCount[1]">已确认</el-badge>
          </el-radio-button>
          <el-radio-button label="2">
            <el-badge :value="statusCount[2]" :hidden="!statusCount[2]">已取车</el-badge>
          </el-radio-button>
          <el-radio-button label="3">
            <el-badge :value="statusCount[3]" :hidden="!statusCount[3]">已还车</el-badge>
          </el-radio-button>
          <el-radio-button label="4">
            <el-badge :value="statusCount[4]" :hidden="!statusCount[4]">已完成</el-badge>
          </el-radio-button>
          <el-radio-button label="5">
            <el-badge :value="statusCount[5]" :hidden="!statusCount[5]">已取消</el-badge>
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 订单列表 - 卡片式设计 -->
      <div class="orders-list" v-loading="loading">
        <div class="order-card" v-for="order in orders" :key="order.id">
          <div class="order-header">
            <div class="order-info">
              <span class="order-no">订单号: {{ order.orderNo }}</span>
              <span class="order-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(order.createTime) }}
              </span>
            </div>
            <div class="order-status" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </div>
          </div>

          <div class="order-body">
            <div class="car-info">
              <div class="car-image">
                <el-image :src="order.carImage || defaultImage" fit="cover" />
              </div>
              <div class="car-details">
                <h4>{{ order.carName }}</h4>
                <div class="car-meta">
                  <el-tag size="small" type="info">{{ order.carBrand }}</el-tag>
                  <el-tag size="small" type="info">{{ order.carCategory }}</el-tag>
                </div>
                <div class="rental-info">
                  <div class="info-item">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDateTime(order.startTime) }} - {{ formatDateTime(order.endTime) }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Timer /></el-icon>
                    <span>共 {{ calculateDays(order.startTime, order.endTime) }} 天</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Location /></el-icon>
                    <span>取车: {{ order.pickupStoreName }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Location /></el-icon>
                    <span>还车: {{ order.returnStoreName }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="order-price">
              <div class="price-item">
                <span class="label">租金</span>
                <span class="value">¥{{ formatPrice(order.totalPrice) }}</span>
              </div>
              <div class="price-item" v-if="order.deposit">
                <span class="label">押金</span>
                <span class="value">¥{{ formatPrice(order.deposit) }}</span>
              </div>
            </div>
          </div>

          <div class="order-footer">
            <div class="user-info">
              <el-avatar :size="28" :src="order.userAvatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-details">
                <span class="user-name">{{ order.userName || '未知用户' }}</span>
                <span class="user-phone" v-if="order.userPhone">{{ order.userPhone }}</span>
              </div>
            </div>
            <div class="total-price">
              合计: <span class="amount">¥{{ formatPrice(order.totalPrice) }}</span>
            </div>
            <div class="order-actions">
              <!-- 确认订单按钮 - 待确认状态 -->
              <el-button
                v-if="order.status === 0 && hasPermission('confirm')"
                type="primary"
                size="default"
                @click="handleConfirmOrder(order)"
              >
                <el-icon><Check /></el-icon>
                确认订单
              </el-button>
              <!-- 拒绝订单按钮 - 待确认状态 -->
              <el-button
                v-if="order.status === 0 && hasPermission('reject')"
                type="danger"
                size="default"
                @click="handleRejectOrder(order)"
              >
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
              <!-- 确认取车按钮 - 已确认状态 -->
              <el-button
                v-if="order.status === 1 && hasPermission('pickup')"
                type="primary"
                size="default"
                @click="handleConfirmPickup(order)"
              >
                <el-icon><Check /></el-icon>
                确认取车
              </el-button>
              <!-- 确认还车按钮 - 已取车状态 -->
              <el-button
                v-if="order.status === 2 && hasPermission('return')"
                type="success"
                size="default"
                @click="handleConfirmReturn(order)"
              >
                <el-icon><CircleCheck /></el-icon>
                确认还车
              </el-button>
              <!-- 结算按钮 - 已还车状态 -->
              <el-button
                v-if="order.status === 3 && hasPermission('settle')"
                type="warning"
                size="default"
                @click="handleSettleOrder(order)"
              >
                <el-icon><Money /></el-icon>
                结算
              </el-button>
              <!-- 取消按钮 - 非终态 -->
              <el-button
                v-if="order.status !== 4 && order.status !== 5 && hasPermission('cancel')"
                type="info"
                size="default"
                @click="handleCancelOrder(order)"
              >
                <el-icon><Close /></el-icon>
                取消
              </el-button>
              <el-button size="default" @click="viewOrderDetail(order)">
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && orders.length === 0" description="暂无订单记录" />
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 确认取车对话框 -->
    <el-dialog v-model="pickupDialogVisible" title="确认取车" width="550px" destroy-on-close>
      <div class="confirm-dialog-content">
        <div class="order-summary">
          <div class="summary-item">
            <span class="label">订单号:</span>
            <span class="value">{{ currentOrder?.orderNo }}</span>
          </div>
          <div class="summary-item">
            <span class="label">车辆:</span>
            <span class="value">{{ currentOrder?.carName }}</span>
          </div>
          <div class="summary-item">
            <span class="label">用户:</span>
            <span class="value">{{ currentOrder?.userName || currentOrder?.userPhone }}</span>
          </div>
          <div class="summary-item">
            <span class="label">取车门店:</span>
            <span class="value">{{ currentOrder?.pickupStoreName }}</span>
          </div>
          <div class="summary-item">
            <span class="label">取车时间:</span>
            <span class="value">{{ formatDateTime(currentOrder?.startTime) }}</span>
          </div>
        </div>
        <el-divider />
        <el-form ref="pickupFormRef" :model="pickupForm" :rules="pickupRules" label-width="100px">
          <el-form-item label="操作人" prop="operatorName">
            <el-input v-model="pickupForm.operatorName" placeholder="请输入操作人姓名" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input 
              v-model="pickupForm.remark" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入备注信息（可选）" 
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="pickupDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPickup" :loading="pickupLoading">确认取车</el-button>
      </template>
    </el-dialog>

    <!-- 确认还车对话框 -->
    <el-dialog v-model="returnDialogVisible" title="确认还车" width="550px" destroy-on-close>
      <div class="confirm-dialog-content">
        <div class="order-summary">
          <div class="summary-item">
            <span class="label">订单号:</span>
            <span class="value">{{ currentOrder?.orderNo }}</span>
          </div>
          <div class="summary-item">
            <span class="label">车辆:</span>
            <span class="value">{{ currentOrder?.carName }}</span>
          </div>
          <div class="summary-item">
            <span class="label">用户:</span>
            <span class="value">{{ currentOrder?.userName || currentOrder?.userPhone }}</span>
          </div>
          <div class="summary-item">
            <span class="label">还车门店:</span>
            <span class="value">{{ currentOrder?.returnStoreName }}</span>
          </div>
          <div class="summary-item">
            <span class="label">还车时间:</span>
            <span class="value">{{ formatDateTime(currentOrder?.endTime) }}</span>
          </div>
        </div>
        <el-divider />
        <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-width="100px">
          <el-form-item label="操作人" prop="operatorName">
            <el-input v-model="returnForm.operatorName" placeholder="请输入操作人姓名" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input 
              v-model="returnForm.remark" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入备注信息（可选）" 
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="success" @click="confirmReturn" :loading="returnLoading">确认还车</el-button>
      </template>
    </el-dialog>

    <!-- 确认订单对话框 -->
    <el-dialog v-model="confirmDialogVisible" title="确认订单" width="550px" destroy-on-close>
      <div class="confirm-dialog-content">
        <div class="order-summary">
          <div class="summary-item">
            <span class="label">订单号:</span>
            <span class="value">{{ currentOrder?.orderNo }}</span>
          </div>
          <div class="summary-item">
            <span class="label">车辆:</span>
            <span class="value">{{ currentOrder?.carName }}</span>
          </div>
          <div class="summary-item">
            <span class="label">用户:</span>
            <span class="value">{{ currentOrder?.userName || currentOrder?.userPhone }}</span>
          </div>
          <div class="summary-item">
            <span class="label">租车时间:</span>
            <span class="value">{{ formatDateTime(currentOrder?.startTime) }} 至 {{ formatDateTime(currentOrder?.endTime) }}</span>
          </div>
        </div>
        <el-divider />
        <el-form ref="confirmFormRef" :model="confirmForm" :rules="confirmRules" label-width="100px">
          <el-form-item label="操作人" prop="operatorName">
            <el-input v-model="confirmForm.operatorName" placeholder="请输入操作人姓名" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input 
              v-model="confirmForm.remark" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入备注信息（可选）" 
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfirmOrder" :loading="confirmLoading">确认订单</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝订单对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝订单" width="550px" destroy-on-close>
      <div class="confirm-dialog-content">
        <div class="order-summary">
          <div class="summary-item">
            <span class="label">订单号:</span>
            <span class="value">{{ currentOrder?.orderNo }}</span>
          </div>
          <div class="summary-item">
            <span class="label">车辆:</span>
            <span class="value">{{ currentOrder?.carName }}</span>
          </div>
          <div class="summary-item">
            <span class="label">用户:</span>
            <span class="value">{{ currentOrder?.userName || currentOrder?.userPhone }}</span>
          </div>
        </div>
        <el-divider />
        <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="100px">
          <el-form-item label="拒绝原因" prop="reasonCode">
            <el-select v-model="rejectForm.reasonCode" placeholder="请选择拒绝原因" style="width: 100%">
              <el-option 
                v-for="reason in cancelReasons" 
                :key="reason.reasonCode" 
                :label="reason.reasonName" 
                :value="reason.reasonCode" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="操作人" prop="operatorName">
            <el-input v-model="rejectForm.operatorName" placeholder="请输入操作人姓名" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input 
              v-model="rejectForm.remark" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入备注信息（可选）" 
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitRejectOrder" :loading="rejectLoading">拒绝订单</el-button>
      </template>
    </el-dialog>

    <!-- 结算订单对话框 -->
    <el-dialog v-model="settleDialogVisible" title="订单结算" width="550px" destroy-on-close>
      <div class="confirm-dialog-content">
        <div class="order-summary">
          <div class="summary-item">
            <span class="label">订单号:</span>
            <span class="value">{{ currentOrder?.orderNo }}</span>
          </div>
          <div class="summary-item">
            <span class="label">车辆:</span>
            <span class="value">{{ currentOrder?.carName }}</span>
          </div>
          <div class="summary-item">
            <span class="label">用户:</span>
            <span class="value">{{ currentOrder?.userName || currentOrder?.userPhone }}</span>
          </div>
          <div class="summary-item">
            <span class="label">原订单金额:</span>
            <span class="value">¥{{ formatPrice(currentOrder?.totalPrice) }}</span>
          </div>
        </div>
        <el-divider />
        <el-form ref="settleFormRef" :model="settleForm" :rules="settleRules" label-width="120px">
          <el-form-item label="结算金额" prop="settlementAmount">
            <el-input-number 
              v-model="settleForm.settlementAmount" 
              :min="0" 
              :precision="2" 
              style="width: 100%"
              placeholder="不填则自动计算"
            />
          </el-form-item>
          <el-form-item label="操作人" prop="operatorName">
            <el-input v-model="settleForm.operatorName" placeholder="请输入操作人姓名" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input 
              v-model="settleForm.remark" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入备注信息（可选）" 
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="settleDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="submitSettleOrder" :loading="settleLoading">确认结算</el-button>
      </template>
    </el-dialog>

    <!-- 取消订单对话框 -->
    <el-dialog v-model="cancelDialogVisible" title="取消订单" width="550px" destroy-on-close>
      <div class="confirm-dialog-content">
        <div class="order-summary">
          <div class="summary-item">
            <span class="label">订单号:</span>
            <span class="value">{{ currentOrder?.orderNo }}</span>
          </div>
          <div class="summary-item">
            <span class="label">车辆:</span>
            <span class="value">{{ currentOrder?.carName }}</span>
          </div>
          <div class="summary-item">
            <span class="label">用户:</span>
            <span class="value">{{ currentOrder?.userName || currentOrder?.userPhone }}</span>
          </div>
        </div>
        <el-divider />
        <el-form ref="cancelFormRef" :model="cancelForm" :rules="cancelRules" label-width="100px">
          <el-form-item label="取消原因" prop="reasonCode">
            <el-select v-model="cancelForm.reasonCode" placeholder="请选择取消原因" style="width: 100%">
              <el-option 
                v-for="reason in cancelReasons" 
                :key="reason.reasonCode" 
                :label="reason.reasonName" 
                :value="reason.reasonCode" 
              />
            </el-select>
          </el-form-item>
          <el-form-item label="详细说明" prop="reasonDetail">
            <el-input 
              v-model="cancelForm.reasonDetail" 
              type="textarea" 
              :rows="3" 
              placeholder="请输入详细说明（可选）" 
            />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input 
              v-model="cancelForm.remark" 
              type="textarea" 
              :rows="2" 
              placeholder="请输入备注信息（可选）" 
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitCancelOrder" :loading="cancelLoading">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="900px" destroy-on-close>
      <div v-if="orderDetail" class="order-detail-container">
        <!-- 订单状态流程 -->
        <div class="status-timeline">
          <el-steps :active="getStatusStep(orderDetail.status)" finish-status="success">
            <el-step title="待确认" description="订单创建" />
            <el-step title="已确认" description="等待取车" />
            <el-step title="已取车" description="用车中" />
            <el-step title="已还车" description="待结算" />
            <el-step title="已完成" description="订单完成" />
          </el-steps>
        </div>

        <!-- 用户信息卡片 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>用户信息</span>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="用户名">{{ orderDetail.userName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ orderDetail.userPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ orderDetail.userEmail || '-' }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ orderDetail.userRealName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="证件类型">{{ orderDetail.userIdCardTypeText || '身份证' }}</el-descriptions-item>
            <el-descriptions-item label="证件号码">{{ orderDetail.userIdCard || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 车辆信息卡片 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon><Van /></el-icon>
              <span>车辆信息</span>
            </div>
          </template>
          <div class="car-detail-content">
            <el-image :src="orderDetail.carImage || defaultImage" fit="cover" class="car-detail-image" />
            <div class="car-detail-info">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="车辆名称">{{ orderDetail.carName }}</el-descriptions-item>
                <el-descriptions-item label="品牌">{{ orderDetail.carBrand }}</el-descriptions-item>
                <el-descriptions-item label="车型">{{ orderDetail.carCategory }}</el-descriptions-item>
                <el-descriptions-item label="颜色">{{ orderDetail.carColor }}</el-descriptions-item>
                <el-descriptions-item label="座位数">{{ orderDetail.carSeats }}座</el-descriptions-item>
                <el-descriptions-item label="变速箱">{{ orderDetail.carTransmission }}</el-descriptions-item>
                <el-descriptions-item label="燃油类型">{{ orderDetail.carFuelType }}</el-descriptions-item>
                <el-descriptions-item label="车牌号">{{ orderDetail.carPlateNumber || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-card>

        <!-- 租赁信息卡片 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon><Calendar /></el-icon>
              <span>租赁信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="预约取车时间">{{ formatDateTime(orderDetail.startTime) }}</el-descriptions-item>
            <el-descriptions-item label="预约还车时间">{{ formatDateTime(orderDetail.endTime) }}</el-descriptions-item>
            <el-descriptions-item label="预约租车天数">{{ orderDetail.rentalDays }} 天</el-descriptions-item>
            <el-descriptions-item label="日租金">¥{{ formatPrice(orderDetail.dailyRate) }}</el-descriptions-item>
            <el-descriptions-item label="取车门店">{{ orderDetail.pickupStoreName }}</el-descriptions-item>
            <el-descriptions-item label="还车门店">{{ orderDetail.returnStoreName }}</el-descriptions-item>
          </el-descriptions>
          
          <!-- 实际租赁信息（取还车后显示） -->
          <el-divider v-if="orderDetail.pickupTime || orderDetail.returnTime" content-position="left">实际租赁信息</el-divider>
          <el-descriptions v-if="orderDetail.pickupTime || orderDetail.returnTime" :column="2" border>
            <el-descriptions-item label="实际取车时间">
              <span v-if="orderDetail.pickupTime" class="actual-time">{{ formatDateTime(orderDetail.pickupTime) }}</span>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="实际还车时间">
              <span v-if="orderDetail.returnTime" class="actual-time">{{ formatDateTime(orderDetail.returnTime) }}</span>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="实际使用天数" v-if="orderDetail.actualRentalDays">
              <span class="highlight">{{ orderDetail.actualRentalDays }} 天</span>
              <el-tag v-if="orderDetail.actualRentalDays !== orderDetail.rentalDays" 
                      :type="orderDetail.actualRentalDays > orderDetail.rentalDays ? 'danger' : 'success'" 
                      size="small" style="margin-left: 8px;">
                {{ orderDetail.actualRentalDays > orderDetail.rentalDays ? '超时' : '提前' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 预约费用明细卡片 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <el-icon><Money /></el-icon>
              <span>预约费用明细</span>
            </div>
          </template>
          <div class="fee-list">
            <div class="fee-item">
              <span class="fee-name">租金</span>
              <span class="fee-desc">{{ orderDetail.rentalDays }}天 × ¥{{ formatPrice(orderDetail.dailyRate) }}/天</span>
              <span class="fee-amount">¥{{ formatPrice(orderDetail.rentalFee) }}</span>
            </div>
            <div v-if="orderDetail.insuranceFee > 0" class="fee-item">
              <span class="fee-name">保险费用</span>
              <span class="fee-desc"></span>
              <span class="fee-amount">¥{{ formatPrice(orderDetail.insuranceFee) }}</span>
            </div>
            <div v-if="orderDetail.serviceFee > 0" class="fee-item">
              <span class="fee-name">服务费用</span>
              <span class="fee-desc"></span>
              <span class="fee-amount">¥{{ formatPrice(orderDetail.serviceFee) }}</span>
            </div>
            <div v-if="orderDetail.discountAmount > 0" class="fee-item discount">
              <span class="fee-name">优惠折扣</span>
              <span class="fee-desc"></span>
              <span class="fee-amount negative">-¥{{ formatPrice(orderDetail.discountAmount) }}</span>
            </div>
            <el-divider />
            <div class="fee-total">
              <span class="total-label">预约总价</span>
              <span class="total-amount">¥{{ formatPrice(orderDetail.totalPrice) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 实际费用明细卡片（结算后显示） -->
        <el-card class="detail-card actual-fee-card" v-if="orderDetail.status === 4 && orderDetail.settlementAmount !== null">
          <template #header>
            <div class="card-header">
              <el-icon><Money /></el-icon>
              <span>实际费用明细（结算）</span>
            </div>
          </template>
          <div class="fee-list">
            <div class="fee-item">
              <span class="fee-name">实际租金</span>
              <span class="fee-desc">{{ orderDetail.actualRentalDays }}天 × ¥{{ formatPrice(orderDetail.dailyRate) }}/天</span>
              <span class="fee-amount" :class="{ 'adjusted': orderDetail.actualRentalFee !== orderDetail.rentalFee }">
                ¥{{ formatPrice(orderDetail.actualRentalFee) }}
                <span v-if="orderDetail.actualRentalFee !== orderDetail.rentalFee" class="adjust-hint">
                  (原¥{{ formatPrice(orderDetail.rentalFee) }})
                </span>
              </span>
            </div>
            <div v-if="orderDetail.actualInsuranceFee > 0" class="fee-item">
              <span class="fee-name">实际保险费用</span>
              <span class="fee-desc"></span>
              <span class="fee-amount">¥{{ formatPrice(orderDetail.actualInsuranceFee) }}</span>
            </div>
            <div v-if="orderDetail.actualServiceFee > 0" class="fee-item">
              <span class="fee-name">实际服务费用</span>
              <span class="fee-desc"></span>
              <span class="fee-amount">¥{{ formatPrice(orderDetail.actualServiceFee) }}</span>
            </div>
            <div v-if="orderDetail.actualOvertimeFee > 0" class="fee-item overtime">
              <span class="fee-name">超时费用</span>
              <span class="fee-desc">实际还车时间超过预约时间</span>
              <span class="fee-amount">¥{{ formatPrice(orderDetail.actualOvertimeFee) }}</span>
            </div>
            <div v-if="orderDetail.mileageFee > 0" class="fee-item">
              <span class="fee-name">超里程费用</span>
              <span class="fee-desc"></span>
              <span class="fee-amount">¥{{ formatPrice(orderDetail.mileageFee) }}</span>
            </div>
            <div v-if="orderDetail.discountAmount > 0" class="fee-item discount">
              <span class="fee-name">优惠折扣</span>
              <span class="fee-desc"></span>
              <span class="fee-amount negative">-¥{{ formatPrice(orderDetail.discountAmount) }}</span>
            </div>
            <el-divider />
            <div class="fee-total settlement">
              <span class="total-label">结算金额</span>
              <span class="total-amount">¥{{ formatPrice(orderDetail.settlementAmount) }}</span>
            </div>
            <div v-if="orderDetail.settlementAmount < orderDetail.totalPrice" class="fee-total refund">
              <span class="total-label">应退金额</span>
              <span class="total-amount">¥{{ formatPrice(orderDetail.totalPrice - orderDetail.settlementAmount) }}</span>
            </div>
            <div v-if="orderDetail.settlementAmount > orderDetail.totalPrice" class="fee-total extra">
              <span class="total-label">补交金额</span>
              <span class="total-amount">¥{{ formatPrice(orderDetail.settlementAmount - orderDetail.totalPrice) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 取还车记录卡片 -->
        <el-card class="detail-card" v-if="orderDetail.pickupTime || orderDetail.returnTime">
          <template #header>
            <div class="card-header">
              <el-icon><DocumentChecked /></el-icon>
              <span>取还车记录</span>
            </div>
          </template>
          <div v-if="orderDetail.pickupTime" class="record-item">
            <div class="record-header">
              <el-tag type="primary">取车确认</el-tag>
              <span class="record-time">{{ formatDateTime(orderDetail.pickupTime) }}</span>
            </div>
            <div class="record-content">
              <p><strong>操作人:</strong> {{ orderDetail.pickupOperator }}</p>
              <p v-if="orderDetail.pickupRemark"><strong>备注:</strong> {{ orderDetail.pickupRemark }}</p>
            </div>
          </div>
          <el-divider v-if="orderDetail.pickupTime && orderDetail.returnTime" />
          <div v-if="orderDetail.returnTime" class="record-item">
            <div class="record-header">
              <el-tag type="success">还车确认</el-tag>
              <span class="record-time">{{ formatDateTime(orderDetail.returnTime) }}</span>
            </div>
            <div class="record-content">
              <p><strong>操作人:</strong> {{ orderDetail.returnOperator }}</p>
              <p v-if="orderDetail.returnRemark"><strong>备注:</strong> {{ orderDetail.returnRemark }}</p>
            </div>
          </div>
        </el-card>

        <!-- 状态变更日志卡片 -->
        <el-card class="detail-card" v-if="orderDetail.statusLogs && orderDetail.statusLogs.length > 0">
          <template #header>
            <div class="card-header">
              <el-icon><List /></el-icon>
              <span>状态变更日志</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(log, index) in orderDetail.statusLogs"
              :key="index"
              :type="index === 0 ? 'primary' : ''"
              :timestamp="formatDateTime(log.createTime)"
            >
              <div class="log-item">
                <p class="log-title">
                  <el-tag size="small">{{ log.fromStatusText }}</el-tag>
                  <el-icon class="log-arrow"><ArrowRight /></el-icon>
                  <el-tag size="small" type="success">{{ log.toStatusText }}</el-tag>
                </p>
                <p class="log-operator">
                  <el-icon><User /></el-icon>
                  {{ log.operatorName }}
                </p>
                <p v-if="log.remark" class="log-remark">备注: {{ log.remark }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRoute, onBeforeRouteUpdate } from 'vue-router'
import { adminApi } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Calendar, Timer, Location, User, Check, CircleCheck, Close,
  View, Clock, Document, Phone, Search, Van, Money,
  DocumentChecked, List, ArrowRight
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const orders = ref([])
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const defaultImage = 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=400&h=300&fit=crop'
const storeList = ref([])
const statusCount = ref({})

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  userName: '',
  userPhone: '',
  status: '',
  timeRange: '',
  dateRange: [],
  storeId: ''
})

// 对话框状态
const pickupDialogVisible = ref(false)
const returnDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const rejectDialogVisible = ref(false)
const settleDialogVisible = ref(false)
const cancelDialogVisible = ref(false)
const pickupLoading = ref(false)
const returnLoading = ref(false)
const confirmLoading = ref(false)
const rejectLoading = ref(false)
const settleLoading = ref(false)
const cancelLoading = ref(false)
const pickupFormRef = ref(null)
const returnFormRef = ref(null)
const confirmFormRef = ref(null)
const rejectFormRef = ref(null)
const settleFormRef = ref(null)
const cancelFormRef = ref(null)
const currentOrder = ref(null)
const orderDetail = ref(null)
const cancelReasons = ref([])

const pickupForm = reactive({
  operatorName: userStore.user?.username || '',
  remark: ''
})

const returnForm = reactive({
  operatorName: userStore.user?.username || '',
  remark: ''
})

const confirmForm = reactive({
  operatorName: userStore.user?.username || '',
  remark: ''
})

const rejectForm = reactive({
  operatorName: userStore.user?.username || '',
  reasonCode: '',
  remark: ''
})

const settleForm = reactive({
  operatorName: userStore.user?.username || '',
  settlementAmount: null,
  remark: ''
})

const cancelForm = reactive({
  reasonCode: '',
  reasonDetail: '',
  remark: ''
})

const pickupRules = {
  operatorName: [{ required: true, message: '请输入操作人姓名', trigger: 'blur' }]
}

const returnRules = {
  operatorName: [{ required: true, message: '请输入操作人姓名', trigger: 'blur' }]
}

const confirmRules = {
  operatorName: [{ required: true, message: '请输入操作人姓名', trigger: 'blur' }]
}

const rejectRules = {
  operatorName: [{ required: true, message: '请输入操作人姓名', trigger: 'blur' }],
  reasonCode: [{ required: true, message: '请选择拒绝原因', trigger: 'change' }]
}

const settleRules = {
  operatorName: [{ required: true, message: '请输入操作人姓名', trigger: 'blur' }]
}

const cancelRules = {
  reasonCode: [{ required: true, message: '请选择取消原因', trigger: 'change' }]
}

// 权限检查
const hasPermission = (action) => {
  const role = userStore.user?.role
  // 0-普通用户，1-管理员，2-门店管理员，3-客服，4-财务
  switch (action) {
    case 'confirm':
      return role === 1 || role === 2 || role === 3 // 管理员、门店管理员、客服
    case 'reject':
      return role === 1 || role === 2 || role === 3 // 管理员、门店管理员、客服
    case 'pickup':
      return role === 1 || role === 2 || role === 3 // 管理员、门店管理员、客服
    case 'return':
      return role === 1 || role === 2 || role === 3 // 管理员、门店管理员、客服
    case 'settle':
      return role === 1 || role === 4 // 管理员、财务
    case 'cancel':
      return role === 1 || role === 2 || role === 3 // 管理员、门店管理员、客服
    default:
      return role === 1 || role === 2
  }
}

const getStatusClass = (status) => {
  const classes = {
    0: 'status-pending',
    1: 'status-confirmed',
    2: 'status-pickup',
    3: 'status-returned',
    4: 'status-completed',
    5: 'status-cancelled'
  }
  return classes[status] || ''
}

const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'primary',
    2: 'info',
    3: 'success',
    4: 'success',
    5: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '待确认',
    1: '已确认',
    2: '已取车',
    3: '已还车',
    4: '已完成',
    5: '已取消'
  }
  return texts[status] || '未知'
}

const getStatusStep = (status) => {
  const steps = {
    0: 0,
    1: 1,
    2: 2,
    3: 3,
    4: 4,
    5: 0
  }
  return steps[status] || 0
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const formatDateTime = (time) => {
  if (!time) return '-'
  return formatTime(time)
}

const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}

const calculateDays = (startTime, endTime) => {
  if (!startTime || !endTime) return 0
  const start = new Date(startTime)
  const end = new Date(endTime)
  const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 1
}

const handleFilterChange = () => {
  currentPage.value = 1
  searchForm.status = statusFilter.value
  loadOrders()
}

const handleSearch = () => {
  currentPage.value = 1
  loadOrders()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.userName = ''
  searchForm.userPhone = ''
  searchForm.status = ''
  searchForm.timeRange = ''
  searchForm.dateRange = []
  searchForm.storeId = ''
  statusFilter.value = ''
  currentPage.value = 1
  loadOrders()
}

const handleDateRangeChange = (val) => {
  if (val && val.length === 2) {
    searchForm.createTimeBegin = val[0]
    searchForm.createTimeEnd = val[1]
  } else {
    searchForm.createTimeBegin = null
    searchForm.createTimeEnd = null
  }
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadOrders()
}

const handlePageChange = (val) => {
  currentPage.value = val
  loadOrders()
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      orderNo: searchForm.orderNo || undefined,
      userName: searchForm.userName || undefined,
      userPhone: searchForm.userPhone || undefined,
      status: searchForm.status || undefined,
      timeRange: searchForm.timeRange || undefined,
      storeId: searchForm.storeId || undefined
    }
    
    // 添加自定义时间范围
    if (searchForm.timeRange === 'custom' && searchForm.dateRange?.length === 2) {
      params.createTimeBegin = searchForm.dateRange[0]
      params.createTimeEnd = searchForm.dateRange[1]
    }
    
    const data = await adminApi.getOrders(params)
    orders.value = data?.records || []
    total.value = data?.total || 0
    
    // 计算各状态订单数量
    calculateStatusCount()
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const calculateStatusCount = () => {
  const counts = { 0: 0, 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  orders.value.forEach(order => {
    if (counts[order.status] !== undefined) {
      counts[order.status]++
    }
  })
  statusCount.value = counts
}

// 加载门店列表
const loadStores = async () => {
  try {
    const data = await adminApi.getStores({ page: 1, size: 100 })
    storeList.value = data?.records || []
  } catch (error) {
    console.error('加载门店列表失败:', error)
  }
}

// 确认订单
const handleConfirmOrder = (order) => {
  currentOrder.value = order
  confirmForm.operatorName = userStore.user?.username || ''
  confirmForm.remark = ''
  confirmDialogVisible.value = true
}

const submitConfirmOrder = async () => {
  const valid = await confirmFormRef.value.validate().catch(() => false)
  if (!valid) return

  confirmLoading.value = true
  try {
    await adminApi.confirmOrder(currentOrder.value.id, {
      operatorName: confirmForm.operatorName,
      remark: confirmForm.remark
    })
    ElMessage.success('订单确认成功')
    confirmDialogVisible.value = false
    loadOrders()
  } catch (error) {
    console.error('订单确认失败:', error)
    ElMessage.error(error.message || '订单确认失败')
  } finally {
    confirmLoading.value = false
  }
}

// 拒绝订单
const handleRejectOrder = (order) => {
  currentOrder.value = order
  rejectForm.operatorName = userStore.user?.username || ''
  rejectForm.reasonCode = ''
  rejectForm.remark = ''
  loadCancelReasons()
  rejectDialogVisible.value = true
}

const submitRejectOrder = async () => {
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return

  rejectLoading.value = true
  try {
    await adminApi.rejectOrder(currentOrder.value.id, {
      operatorName: rejectForm.operatorName,
      reasonCode: rejectForm.reasonCode,
      remark: rejectForm.remark
    })
    ElMessage.success('订单已拒绝')
    rejectDialogVisible.value = false
    loadOrders()
  } catch (error) {
    console.error('拒绝订单失败:', error)
    ElMessage.error(error.message || '拒绝订单失败')
  } finally {
    rejectLoading.value = false
  }
}

// 确认取车
const handleConfirmPickup = (order) => {
  currentOrder.value = order
  pickupForm.operatorName = userStore.user?.username || ''
  pickupForm.remark = ''
  pickupDialogVisible.value = true
}

const confirmPickup = async () => {
  const valid = await pickupFormRef.value.validate().catch(() => false)
  if (!valid) return

  pickupLoading.value = true
  try {
    await adminApi.confirmPickup(currentOrder.value.id, {
      operatorName: pickupForm.operatorName,
      remark: pickupForm.remark
    })
    ElMessage.success('确认取车成功')
    pickupDialogVisible.value = false
    loadOrders()
  } catch (error) {
    console.error('确认取车失败:', error)
    ElMessage.error(error.message || '确认取车失败')
  } finally {
    pickupLoading.value = false
  }
}

// 确认还车
const handleConfirmReturn = (order) => {
  currentOrder.value = order
  returnForm.operatorName = userStore.user?.username || ''
  returnForm.remark = ''
  returnDialogVisible.value = true
}

const confirmReturn = async () => {
  const valid = await returnFormRef.value.validate().catch(() => false)
  if (!valid) return

  returnLoading.value = true
  try {
    await adminApi.confirmReturn(currentOrder.value.id, {
      operatorName: returnForm.operatorName,
      remark: returnForm.remark
    })
    ElMessage.success('确认还车成功')
    returnDialogVisible.value = false
    loadOrders()
  } catch (error) {
    console.error('确认还车失败:', error)
    ElMessage.error(error.message || '确认还车失败')
  } finally {
    returnLoading.value = false
  }
}

// 结算订单
const handleSettleOrder = (order) => {
  currentOrder.value = order
  settleForm.operatorName = userStore.user?.username || ''
  settleForm.settlementAmount = null
  settleForm.remark = ''
  settleDialogVisible.value = true
}

const submitSettleOrder = async () => {
  const valid = await settleFormRef.value.validate().catch(() => false)
  if (!valid) return

  settleLoading.value = true
  try {
    await adminApi.settleOrder(currentOrder.value.id, {
      settlementAmount: settleForm.settlementAmount,
      operatorName: settleForm.operatorName,
      remark: settleForm.remark
    })
    ElMessage.success('订单结算成功')
    settleDialogVisible.value = false
    loadOrders()
  } catch (error) {
    console.error('订单结算失败:', error)
    ElMessage.error(error.message || '订单结算失败')
  } finally {
    settleLoading.value = false
  }
}

// 取消订单
const handleCancelOrder = (order) => {
  currentOrder.value = order
  cancelForm.reasonCode = ''
  cancelForm.reasonDetail = ''
  cancelForm.remark = ''
  loadCancelReasons()
  cancelDialogVisible.value = true
}

const submitCancelOrder = async () => {
  const valid = await cancelFormRef.value.validate().catch(() => false)
  if (!valid) return

  cancelLoading.value = true
  try {
    await adminApi.cancelOrder(currentOrder.value.id, {
      reasonCode: cancelForm.reasonCode,
      reasonDetail: cancelForm.reasonDetail,
      remark: cancelForm.remark
    })
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    loadOrders()
  } catch (error) {
    console.error('取消订单失败:', error)
    ElMessage.error(error.message || '取消订单失败')
  } finally {
    cancelLoading.value = false
  }
}

// 加载取消原因列表
const loadCancelReasons = async () => {
  try {
    const data = await adminApi.getCancelReasons({ reasonType: 3 }) // 管理员取消原因
    cancelReasons.value = data || []
  } catch (error) {
    console.error('加载取消原因失败:', error)
  }
}

// 查看订单详情
const viewOrderDetail = async (order) => {
  try {
    const data = await adminApi.getOrderDetail(order.id)
    orderDetail.value = data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  }
}

onMounted(() => {
  loadOrders()
  loadStores()
})

// 使用导航守卫 - 当路由更新但组件复用时触发
onBeforeRouteUpdate((to, from, next) => {
  if (to.path === '/admin/orders') {
    loadOrders()
  }
  next()
})
</script>

<style lang="scss" scoped>
.admin-orders-page {
  padding: 24px;
  min-height: calc(100vh - 128px);
  background: #f5f7fa;
}

.page-header {
  margin-bottom: 24px;
  background: #fff;
  padding: 20px 24px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

  h1 {
    font-size: 24px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 8px;
  }

  p {
    color: #909399;
    font-size: 14px;
  }
}

.orders-content {
  .search-bar {
    background: #fff;
    padding: 20px 24px;
    border-radius: 12px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    .search-actions {
      display: flex;
      gap: 8px;
    }
  }

  .filter-bar {
    margin-bottom: 20px;
    background: #fff;
    padding: 16px 24px;
    border-radius: 12px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    :deep(.el-radio-button__inner) {
      border-radius: 8px;
      padding: 10px 20px;
      font-weight: 500;
    }
  }

  .orders-list {
    min-height: 300px;

    .order-card {
      background: #fff;
      border-radius: 12px;
      margin-bottom: 16px;
      overflow: hidden;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.1);
        transform: translateY(-2px);
      }

      .order-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 20px;
        background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
        border-bottom: 1px solid #ebeef5;

        .order-info {
          display: flex;
          gap: 16px;
          align-items: center;

          .order-no {
            font-weight: 600;
            color: #303133;
            font-size: 14px;
          }

          .order-time {
            color: #606266;
            font-size: 13px;
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }

        .order-status {
          padding: 6px 16px;
          border-radius: 20px;
          font-size: 13px;
          font-weight: 600;

          &.status-pending {
            background: rgba(230, 162, 60, 0.15);
            color: #e6a23c;
          }

          &.status-confirmed {
            background: rgba(64, 158, 255, 0.15);
            color: #409eff;
          }

          &.status-pickup {
            background: rgba(144, 147, 153, 0.15);
            color: #606266;
          }

          &.status-returned {
            background: rgba(103, 194, 58, 0.15);
            color: #67c23a;
          }

          &.status-completed {
            background: rgba(103, 194, 58, 0.2);
            color: #52c41a;
          }

          &.status-cancelled {
            background: rgba(245, 108, 108, 0.15);
            color: #f56c6c;
          }
        }
      }

      .order-body {
        display: flex;
        padding: 20px;
        gap: 24px;

        @media (max-width: 768px) {
          flex-direction: column;
          gap: 16px;
        }

        .car-info {
          display: flex;
          gap: 16px;
          flex: 1;

          .car-image {
            width: 160px;
            height: 110px;
            border-radius: 8px;
            overflow: hidden;
            flex-shrink: 0;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

            .el-image {
              width: 100%;
              height: 100%;
            }
          }

          .car-details {
            flex: 1;

            h4 {
              font-size: 16px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 8px;
            }

            .car-meta {
              display: flex;
              gap: 8px;
              margin-bottom: 12px;
            }

            .rental-info {
              display: flex;
              flex-direction: column;
              gap: 6px;

              .info-item {
                display: flex;
                align-items: center;
                gap: 6px;
                color: #606266;
                font-size: 13px;

                .el-icon {
                  color: #409eff;
                  font-size: 14px;
                }
              }
            }
          }
        }

        .order-price {
          display: flex;
          flex-direction: column;
          gap: 8px;
          min-width: 140px;
          padding-left: 20px;
          border-left: 1px solid #ebeef5;

          .price-item {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .label {
              color: #909399;
              font-size: 13px;
            }

            .value {
              font-weight: 600;
              color: #303133;
              font-size: 14px;
            }
          }
        }
      }

      .order-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 14px 20px;
        background: #fafafa;
        border-top: 1px solid #ebeef5;

        @media (max-width: 768px) {
          flex-direction: column;
          gap: 12px;
          align-items: flex-start;
        }

        .user-info {
          display: flex;
          align-items: center;
          gap: 10px;

          .user-details {
            display: flex;
            flex-direction: column;
            gap: 2px;

            .user-name {
              color: #303133;
              font-size: 14px;
              font-weight: 500;
            }

            .user-phone {
              color: #909399;
              font-size: 12px;
            }
          }
        }

        .total-price {
          font-size: 14px;
          color: #606266;

          .amount {
            font-size: 20px;
            font-weight: 700;
            color: #f56c6c;
            margin-left: 4px;
          }
        }

        .order-actions {
          display: flex;
          gap: 8px;

          .el-button {
            border-radius: 8px;
            font-weight: 500;
          }
        }
      }
    }
  }

  .pagination {
    margin-top: 24px;
    display: flex;
    justify-content: center;
    background: #fff;
    padding: 16px;
    border-radius: 12px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  }
}

// 确认对话框样式
.confirm-dialog-content {
  .order-summary {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;

    .summary-item {
      display: flex;
      margin-bottom: 10px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        width: 80px;
        color: #606266;
        font-size: 14px;
        flex-shrink: 0;
      }

      .value {
        flex: 1;
        color: #303133;
        font-weight: 500;
        font-size: 14px;

        &.actual-time {
          color: #409eff;
          font-weight: 600;
        }

        &.highlight {
          color: #e6a23c;
          font-weight: 600;
        }
      }
    }
  }
}

// 订单详情样式
.order-detail-container {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 8px;

  .status-timeline {
    margin-bottom: 20px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
  }

  .detail-card {
    margin-bottom: 16px;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #303133;

      .el-icon {
        color: #409eff;
        font-size: 18px;
      }
    }

    .car-detail-content {
      display: flex;
      gap: 20px;

      @media (max-width: 768px) {
        flex-direction: column;
      }

      .car-detail-image {
        width: 200px;
        height: 140px;
        border-radius: 8px;
        flex-shrink: 0;
      }

      .car-detail-info {
        flex: 1;
      }
    }

    .fee-list {
      .fee-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px dashed #ebeef5;

        &.discount {
          .fee-name {
            color: #67c23a;
          }
        }

        .fee-name {
          font-weight: 500;
          color: #303133;
          min-width: 100px;
        }

        .fee-desc {
          flex: 1;
          color: #909399;
          font-size: 13px;
          text-align: center;
        }

        .fee-amount {
          font-weight: 600;
          color: #303133;
          min-width: 100px;
          text-align: right;

          &.negative {
            color: #67c23a;
          }

          &.adjusted {
            color: #e6a23c;
          }

          &.overtime {
            color: #f56c6c;
            font-weight: bold;
          }

          .adjust-hint {
            font-size: 12px;
            color: #909399;
            margin-left: 4px;
          }
        }

        &.overtime {
          .fee-name {
            color: #f56c6c;
            font-weight: bold;
          }
        }
      }

      .fee-total {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 0 8px;

        .total-label {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }

        .total-amount {
          font-size: 24px;
          font-weight: 700;
          color: #f56c6c;
        }

        &.settlement {
          .total-label {
            color: #409eff;
          }
          .total-amount {
            color: #409eff;
          }
        }

        &.refund {
          .total-label {
            color: #67c23a;
          }
          .total-amount {
            color: #67c23a;
          }
        }

        &.extra {
          .total-label {
            color: #f56c6c;
          }
          .total-amount {
            color: #f56c6c;
          }
        }
      }
    }

    .actual-fee-card {
      background: #f5f7fa;
      border: 1px solid #e4e7ed;

      :deep(.el-card__header) {
        background: #ecf5ff;
        border-bottom: 1px solid #d9ecff;
      }
    }

    .record-item {
      padding: 12px 0;

      .record-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;

        .record-time {
          color: #909399;
          font-size: 13px;
        }
      }

      .record-content {
        background: #f5f7fa;
        padding: 12px;
        border-radius: 6px;

        p {
          margin: 4px 0;
          color: #606266;
          font-size: 14px;
        }
      }
    }

    .log-item {
      .log-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 6px;

        .log-arrow {
          color: #909399;
        }
      }

      .log-operator {
        display: flex;
        align-items: center;
        gap: 4px;
        color: #606266;
        font-size: 13px;
        margin-bottom: 4px;
      }

      .log-remark {
        color: #909399;
        font-size: 12px;
        background: #f5f7fa;
        padding: 6px 10px;
        border-radius: 4px;
        margin-top: 6px;
      }
    }
  }
}

:deep(.el-timeline-item__node) {
  background-color: #409eff;
}
</style>
