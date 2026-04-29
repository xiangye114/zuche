<template>
  <div class="orders-page">
    <div class="container">
      <div class="page-header">
        <h1>我的订单</h1>
        <p>查看和管理您的租车订单</p>
      </div>

      <div class="orders-content">
        <div class="filter-bar">
          <el-radio-group v-model="statusFilter" @change="handleFilterChange" size="large">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="0">待确认</el-radio-button>
            <el-radio-button label="1">待取车</el-radio-button>
            <el-radio-button label="2">用车中</el-radio-button>
            <el-radio-button label="3">已还车</el-radio-button>
            <el-radio-button label="4">已完成</el-radio-button>
            <el-radio-button label="5">已取消</el-radio-button>
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
              <div class="car-info" @click="goToCarDetail(order.carId)">
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
                      <span>共 {{ getRentalDays(order.startTime, order.endTime) }} 天</span>
                    </div>
                    <div class="info-item" v-if="order.pickupStoreName">
                      <el-icon><Location /></el-icon>
                      <span>{{ order.pickupStoreName }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="order-price">
                <div class="price-item">
                  <span class="label">租金</span>
                  <span class="value">¥{{ order.totalPrice }}</span>
                </div>
                <div class="price-item" v-if="order.deposit">
                  <span class="label">押金</span>
                  <span class="value">¥{{ order.deposit }}</span>
                </div>
              </div>
            </div>

            <div class="order-footer">
              <div class="total-price">
                合计: <span class="amount">¥{{ order.totalPrice }}</span>
              </div>
              <div class="order-actions">
                <el-button v-if="order.status === 0" type="primary" @click="handlePay(order)">
                  立即支付
                </el-button>
                <el-button v-if="order.status === 0" @click="handleCancel(order)">
                  取消订单
                </el-button>
                <el-button v-if="order.status === 1" type="primary" plain @click="viewOrderDetail(order)">
                  查看详情
                </el-button>
                <el-button v-if="order.status === 2" type="primary" plain @click="viewOrderDetail(order)">
                  查看详情
                </el-button>
                <el-button v-if="order.status === 3" type="primary" plain @click="viewOrderDetail(order)">
                  查看详情
                </el-button>
                <el-button v-if="order.status === 4" type="primary" plain @click="handleReview(order)">
                  去评价
                </el-button>
                <el-button v-if="order.status === 4" type="primary" plain @click="handleRentAgain(order)">
                  再次租赁
                </el-button>
                <el-button v-if="order.status === 5" type="info" plain disabled>
                  已取消
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
    </div>

    <el-dialog v-model="reviewDialogVisible" title="订单评价" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="请输入您的评价" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi } from '@/api/order'
import { reviewApi } from '@/api/review'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Timer, Location } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(false)
const orders = ref([])
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const defaultImage = 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=400&h=300&fit=crop'

const reviewDialogVisible = ref(false)
const reviewForm = ref({
  orderId: null,
  carId: null,
  rating: 5,
  content: ''
})

const getStatusClass = (status) => {
  const classes = {
    0: 'status-pending',
    1: 'status-confirmed',
    2: 'status-using',
    3: 'status-returned',
    4: 'status-completed',
    5: 'status-cancelled'
  }
  return classes[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    0: '待确认',
    1: '已确认',
    2: '用车中',
    3: '已还车',
    4: '已完成',
    5: '已取消'
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

const getRentalDays = (startTime, endTime) => {
  if (!startTime || !endTime) return 0
  const start = new Date(startTime)
  const end = new Date(endTime)
  const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 1
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

const handlePay = async (order) => {
  try {
    await ElMessageBox.confirm('确认支付该订单吗？', '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    })
    await orderApi.pay(order.id)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error(error.message || '支付失败')
    }
  }
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确认取消该订单吗？', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '返回',
      type: 'warning'
    })
    await orderApi.cancel(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
      ElMessage.error(error.message || '取消失败')
    }
  }
}

const handleRentAgain = (order) => {
  router.push({
    path: '/cars',
    query: {
      pickupTime: order.startTime,
      returnTime: order.endTime,
      pickupStoreId: order.pickupStoreId,
      returnStoreId: order.returnStoreId
    }
  })
}

const handleReview = (order) => {
  reviewForm.value = {
    orderId: order.id,
    carId: order.carId,
    rating: 5,
    content: ''
  }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  
  try {
    await reviewApi.create({
      orderId: reviewForm.value.orderId,
      carId: reviewForm.value.carId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    loadOrders()
  } catch (error) {
    console.error('评价失败:', error)
    ElMessage.error(error.message || '评价失败')
  }
}

const goToCarDetail = (carId) => {
  router.push(`/cars/${carId}`)
}

const viewOrderDetail = (order) => {
  router.push(`/orders/${order.id}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.orders-page {
  padding: 32px 0;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 32px;
  
  h1 {
    font-size: 28px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 8px;
  }
  
  p {
    color: $text-tertiary;
    font-size: 15px;
  }
}

.orders-content {
  .filter-bar {
    margin-bottom: 24px;
    
    :deep(.el-radio-button__inner) {
      border-radius: 10px;
      padding: 10px 20px;
      font-weight: 500;
    }
    
    :deep(.el-radio-button:first-child .el-radio-button__inner) {
      border-radius: 10px;
    }
    
    :deep(.el-radio-button:last-child .el-radio-button__inner) {
      border-radius: 10px;
    }
  }
  
  .orders-list {
    min-height: 300px;
    
    .order-card {
      background: $bg-primary;
      border-radius: 16px;
      margin-bottom: 16px;
      overflow: hidden;
      box-shadow: $shadow-card;
      transition: all 0.2s ease;
      
      &:hover {
        box-shadow: $shadow-card-hover;
      }
      
      .order-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 24px;
        background: $bg-tertiary;
        border-bottom: 1px solid $border-light;
        
        .order-info {
          display: flex;
          gap: 24px;
          
          .order-no {
            font-weight: 500;
            color: $text-primary;
          }
          
          .order-time {
            color: $text-tertiary;
            font-size: 14px;
          }
        }
        
        .order-status {
          padding: 6px 16px;
          border-radius: 20px;
          font-size: 13px;
          font-weight: 600;
          
          &.status-pending {
            background: rgba($warning-color, 0.1);
            color: $warning-color;
          }

          &.status-confirmed {
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

          &.status-completed {
            background: rgba($success-color, 0.15);
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
        padding: 24px;
        gap: 32px;
        
        @media (max-width: 768px) {
          flex-direction: column;
          gap: 20px;
        }
        
        .car-info {
          display: flex;
          gap: 20px;
          flex: 1;
          cursor: pointer;
          
          &:hover h4 {
            color: $primary-color;
          }
          
          .car-image {
            width: 160px;
            height: 100px;
            border-radius: 12px;
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
              font-size: 18px;
              font-weight: 600;
              color: $text-primary;
              margin-bottom: 12px;
              transition: color 0.2s ease;
            }
            
            .rental-info {
              display: flex;
              flex-direction: column;
              gap: 8px;
              
              .info-item {
                display: flex;
                align-items: center;
                gap: 8px;
                color: $text-secondary;
                font-size: 14px;
                
                .el-icon {
                  color: $primary-light;
                }
              }
            }
          }
        }
        
        .order-price {
          display: flex;
          flex-direction: column;
          gap: 12px;
          min-width: 140px;
          
          .price-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            
            .label {
              color: $text-tertiary;
              font-size: 14px;
            }
            
            .value {
              font-weight: 600;
              color: $text-primary;
            }
          }
        }
      }
      
      .order-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 24px;
        background: $bg-tertiary;
        border-top: 1px solid $border-light;
        
        .total-price {
          font-size: 15px;
          color: $text-secondary;
          
          .amount {
            font-size: 22px;
            font-weight: 700;
            color: $danger-color;
            margin-left: 4px;
          }
        }
        
        .order-actions {
          display: flex;
          gap: 12px;
          
          .el-button {
            border-radius: 10px;
            font-weight: 500;
          }
        }
      }
    }
  }
  
  .pagination {
    margin-top: 32px;
    display: flex;
    justify-content: center;
  }
}
</style>
