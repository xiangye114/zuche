<template>
  <div class="admin-orders-page">
    <div class="page-header">
      <h1>订单管理</h1>
      <p>管理所有租车订单</p>
    </div>

    <div class="orders-content">
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" @change="handleFilterChange" size="large">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="0">未付款</el-radio-button>
          <el-radio-button label="1">待取车</el-radio-button>
          <el-radio-button label="2">用车中</el-radio-button>
          <el-radio-button label="3">已还车</el-radio-button>
          <el-radio-button label="4">已取消</el-radio-button>
        </el-radio-group>
      </div>

      <div class="orders-list" v-loading="loading">
        <div class="order-card" v-for="order in orders" :key="order.id">
          <div class="order-header">
            <div class="order-info">
              <span class="order-no">订单号: {{ order.orderNo }}</span>
              <span class="order-time">{{ formatTime(order.createTime) }}</span>
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
                <div class="rental-info">
                  <div class="info-item">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDate(order.startTime) }} - {{ formatDate(order.endTime) }}</span>
                  </div>
                  <div class="info-item">
                    <el-icon><Timer /></el-icon>
                    <span>共 {{ order.rentalDays }} 天</span>
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
                <span class="value">¥{{ order.totalAmount }}</span>
              </div>
              <div class="price-item" v-if="order.deposit">
                <span class="label">押金</span>
                <span class="value">¥{{ order.deposit }}</span>
              </div>
            </div>
          </div>

          <div class="order-footer">
            <div class="total-price">
              合计: <span class="amount">¥{{ order.totalAmount }}</span>
            </div>
            <div class="order-actions">
              <el-button v-if="order.status === 1" type="primary" @click="handleConfirmPickup(order)">
                确认取车
              </el-button>
              <el-button v-if="order.status === 2" type="primary" @click="handleConfirmReturn(order)">
                确认还车
              </el-button>
            </div>
          </div>
        </div>

        <el-empty v-if="!loading && orders.length === 0" description="暂无订单记录" />
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadOrders"
        />
      </div>
    </div>

    <el-dialog v-model="pickupDialogVisible" title="确认取车" width="480px">
      <el-form ref="pickupFormRef" :model="pickupForm" :rules="pickupRules" label-width="100px">
        <el-form-item label="操作人" prop="operatorName">
          <el-input v-model="pickupForm.operatorName" placeholder="请输入操作人姓名" size="large" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="pickupForm.remark" type="textarea" placeholder="请输入备注信息" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pickupDialogVisible = false" size="large">取消</el-button>
        <el-button type="primary" @click="confirmPickup" :loading="pickupLoading" size="large">确认取车</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="returnDialogVisible" title="确认还车" width="480px">
      <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-width="100px">
        <el-form-item label="操作人" prop="operatorName">
          <el-input v-model="returnForm.operatorName" placeholder="请输入操作人姓名" size="large" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="returnForm.remark" type="textarea" placeholder="请输入备注信息" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false" size="large">取消</el-button>
        <el-button type="primary" @click="confirmReturn" :loading="returnLoading" size="large">确认还车</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Timer, Location } from '@element-plus/icons-vue'

const loading = ref(false)
const orders = ref([])
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const defaultImage = 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=400&h=300&fit=crop'

const pickupDialogVisible = ref(false)
const returnDialogVisible = ref(false)
const pickupLoading = ref(false)
const returnLoading = ref(false)
const pickupFormRef = ref(null)
const returnFormRef = ref(null)
const currentOrder = ref(null)

const pickupForm = ref({
  operatorName: '',
  remark: ''
})

const returnForm = ref({
  operatorName: '',
  remark: ''
})

const pickupRules = {
  operatorName: [{ required: true, message: '请输入操作人姓名', trigger: 'blur' }]
}

const returnRules = {
  operatorName: [{ required: true, message: '请输入操作人姓名', trigger: 'blur' }]
}

const getStatusClass = (status) => {
  const classes = {
    0: 'status-unpaid',
    1: 'status-waiting',
    2: 'status-using',
    3: 'status-returned',
    4: 'status-cancelled'
  }
  return classes[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    0: '未付款',
    1: '待取车',
    2: '用车中',
    3: '已还车',
    4: '已取消'
  }
  return texts[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadOrders()
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: statusFilter.value
    }
    const res = await orderApi.getList(params)
    orders.value = res?.records || []
    total.value = res?.total || 0
  } finally {
    loading.value = false
  }
}

const handleConfirmPickup = (order) => {
  currentOrder.value = order
  pickupForm.value = {
    operatorName: '',
    remark: ''
  }
  pickupDialogVisible.value = true
}

const handleConfirmReturn = (order) => {
  currentOrder.value = order
  returnForm.value = {
    operatorName: '',
    remark: ''
  }
  returnDialogVisible.value = true
}

const confirmPickup = async () => {
  const valid = await pickupFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  pickupLoading.value = true
  try {
    await orderApi.confirmPickup(currentOrder.value.id, 1, pickupForm.value.operatorName, pickupForm.value.remark)
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

const confirmReturn = async () => {
  const valid = await returnFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  returnLoading.value = true
  try {
    await orderApi.confirmReturn(currentOrder.value.id, 1, returnForm.value.operatorName, returnForm.value.remark)
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

onMounted(() => {
  loadOrders()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.admin-orders-page {
  padding: 24px 0;
  min-height: calc(100vh - 128px);
}

.page-header {
  margin-bottom: 24px;
  
  h1 {
    font-size: 24px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 8px;
  }
  
  p {
    color: $text-tertiary;
    font-size: 14px;
  }
}

.orders-content {
  .filter-bar {
    margin-bottom: 24px;
    
    :deep(.el-radio-button__inner) {
      border-radius: 8px;
      padding: 8px 16px;
      font-weight: 500;
    }
  }
  
  .orders-list {
    min-height: 300px;
    
    .order-card {
      background: $bg-primary;
      border-radius: 12px;
      margin-bottom: 16px;
      overflow: hidden;
      box-shadow: $shadow-card;
      
      .order-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 20px;
        background: $bg-tertiary;
        border-bottom: 1px solid $border-light;
        
        .order-info {
          display: flex;
          gap: 16px;
          
          .order-no {
            font-weight: 500;
            color: $text-primary;
            font-size: 14px;
          }
          
          .order-time {
            color: $text-tertiary;
            font-size: 13px;
          }
        }
        
        .order-status {
          padding: 4px 12px;
          border-radius: 16px;
          font-size: 12px;
          font-weight: 600;
          
          &.status-unpaid {
            background: rgba($warning-color, 0.1);
            color: $warning-color;
          }
          
          &.status-waiting {
            background: rgba($primary-color, 0.1);
            color: $primary-color;
          }
          
          &.status-using {
            background: rgba($info-color, 0.1);
            color: $info-color;
          }
          
          &.status-returned {
            background: rgba($success-color, 0.1);
            color: $success-color;
          }
          
          &.status-cancelled {
            background: rgba($text-tertiary, 0.1);
            color: $text-tertiary;
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
            width: 120px;
            height: 80px;
            border-radius: 8px;
            overflow: hidden;
            flex-shrink: 0;
            
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
              color: $text-primary;
              margin-bottom: 10px;
            }
            
            .rental-info {
              display: flex;
              flex-direction: column;
              gap: 6px;
              
              .info-item {
                display: flex;
                align-items: center;
                gap: 6px;
                color: $text-secondary;
                font-size: 13px;
                
                .el-icon {
                  color: $primary-light;
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
          min-width: 120px;
          
          .price-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            
            .label {
              color: $text-tertiary;
              font-size: 13px;
            }
            
            .value {
              font-weight: 600;
              color: $text-primary;
              font-size: 14px;
            }
          }
        }
      }
      
      .order-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 20px;
        background: $bg-tertiary;
        border-top: 1px solid $border-light;
        
        .total-price {
          font-size: 14px;
          color: $text-secondary;
          
          .amount {
            font-size: 18px;
            font-weight: 700;
            color: $danger-color;
            margin-left: 4px;
          }
        }
        
        .order-actions {
          display: flex;
          gap: 8px;
          
          .el-button {
            border-radius: 8px;
            font-weight: 500;
            font-size: 13px;
          }
        }
      }
    }
  }
  
  .pagination {
    margin-top: 24px;
    display: flex;
    justify-content: center;
  }
}
</style>