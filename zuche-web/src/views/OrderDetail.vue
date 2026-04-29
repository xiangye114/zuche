<template>
  <div class="order-detail container">
    <div class="page-header">
      <el-button type="primary" @click="goBack" size="small">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h1 class="title">订单详情</h1>
    </div>

    <div v-loading="loading" class="detail-content">
      <div v-if="order" class="detail-card card">
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <el-tag :type="getStatusType(order.status)">{{ order.statusText }}</el-tag>
        </div>

        <div class="section">
          <h3 class="section-title">车辆信息</h3>
          <div class="car-info">
            <el-image :src="order.carImage || defaultImage" fit="cover" class="car-image" />
            <div class="car-detail">
              <h4>{{ order.carName }}</h4>
              <p>{{ order.carBrand }} | {{ order.carCategory }}</p>
              <p>{{ order.carColor }} | {{ order.carSeats }}座 | {{ order.carTransmission }} | {{ order.carFuelType }}</p>
              <p>日租金：¥{{ order.carPricePerDay }} | 押金：¥{{ order.carDeposit }}</p>
            </div>
          </div>
        </div>

        <div class="section">
          <h3 class="section-title">租车信息</h3>
          <div class="rent-info">
            <div class="info-item">
              <span class="label">预约取车时间：</span>
              <span class="value">{{ formatTime(order.startTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">预约还车时间：</span>
              <span class="value">{{ formatTime(order.endTime) }}</span>
            </div>
            <div class="info-item" v-if="order.pickupTime">
              <span class="label">实际取车时间：</span>
              <span class="value actual-time">{{ formatTime(order.pickupTime) }}</span>
            </div>
            <div class="info-item" v-if="order.returnTime">
              <span class="label">实际还车时间：</span>
              <span class="value actual-time">{{ formatTime(order.returnTime) }}</span>
            </div>
            <div class="info-item" v-if="order.actualRentalDays">
              <span class="label">实际使用天数：</span>
              <span class="value highlight">{{ order.actualRentalDays }}天</span>
              <span v-if="order.rentalDays !== order.actualRentalDays" class="compare-text">
                (预约{{ order.rentalDays }}天)
              </span>
            </div>
            <div class="info-item">
              <span class="label">取车门店：</span>
              <div class="store-info">
                <div class="store-name">{{ order.pickupStoreName }}</div>
                <div class="store-address">{{ order.pickupStoreAddress }}</div>
                <div class="store-phone">{{ order.pickupStorePhone }}</div>
              </div>
            </div>
            <div class="info-item">
              <span class="label">还车门店：</span>
              <div class="store-info">
                <div class="store-name">{{ order.returnStoreName }}</div>
                <div class="store-address">{{ order.returnStoreAddress }}</div>
                <div class="store-phone">{{ order.returnStorePhone }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 预约费用（订单创建时显示） -->
        <div class="section" v-if="order.status < 4">
          <h3 class="section-title">预约费用</h3>
          <div class="price-info">
            <div class="price-item">
              <span class="label">租金（{{ order.rentalDays }}天 × ¥{{ order.dailyRate }}/天）：</span>
              <span class="value">¥{{ order.rentalFee }}</span>
            </div>
            <div class="price-item" v-if="order.insuranceFee > 0">
              <span class="label">保险费用：</span>
              <span class="value">¥{{ order.insuranceFee }}</span>
            </div>
            <div class="price-item" v-if="order.serviceFee > 0">
              <span class="label">服务费用：</span>
              <span class="value">¥{{ order.serviceFee }}</span>
            </div>
            <div class="price-item" v-if="order.discountAmount > 0">
              <span class="label">优惠折扣：</span>
              <span class="value discount">-¥{{ order.discountAmount }}</span>
            </div>
            <div class="price-item total">
              <span class="label">预约总价：</span>
              <span class="value">¥{{ order.totalPrice }}</span>
            </div>
            <div class="price-item">
              <span class="label">押金：</span>
              <span class="value">¥{{ order.deposit }}</span>
            </div>
          </div>
        </div>

        <!-- 实际费用（结算后显示） -->
        <div class="section actual-fee-section" v-if="order.status === 4 && order.settlementAmount !== null">
          <h3 class="section-title">实际费用明细</h3>
          <div class="price-info">
            <div class="price-item">
              <span class="label">实际租金（{{ order.actualRentalDays }}天 × ¥{{ order.dailyRate }}/天）：</span>
              <span class="value" :class="{ 'adjusted': order.actualRentalFee !== order.rentalFee }">
                ¥{{ order.actualRentalFee }}
                <span v-if="order.actualRentalFee !== order.rentalFee" class="adjust-hint">
                  (原¥{{ order.rentalFee }})
                </span>
              </span>
            </div>
            <div class="price-item" v-if="order.actualInsuranceFee > 0">
              <span class="label">实际保险费用：</span>
              <span class="value">¥{{ order.actualInsuranceFee }}</span>
            </div>
            <div class="price-item" v-if="order.actualServiceFee > 0">
              <span class="label">实际服务费用：</span>
              <span class="value">¥{{ order.actualServiceFee }}</span>
            </div>
            <div class="price-item" v-if="order.actualOvertimeFee > 0">
              <span class="label">超时费用：</span>
              <span class="value overtime">¥{{ order.actualOvertimeFee }}</span>
            </div>
            <div class="price-item" v-if="order.mileageFee > 0">
              <span class="label">超里程费用：</span>
              <span class="value">¥{{ order.mileageFee }}</span>
            </div>
            <div class="price-item" v-if="order.discountAmount > 0">
              <span class="label">优惠折扣：</span>
              <span class="value discount">-¥{{ order.discountAmount }}</span>
            </div>
            <div class="price-item total settlement">
              <span class="label">结算金额：</span>
              <span class="value">¥{{ order.settlementAmount }}</span>
            </div>
            <div class="price-item refund" v-if="order.settlementAmount < order.totalPrice">
              <span class="label">应退金额：</span>
              <span class="value refund-amount">¥{{ (order.totalPrice - order.settlementAmount).toFixed(2) }}</span>
            </div>
            <div class="price-item extra" v-if="order.settlementAmount > order.totalPrice">
              <span class="label">补交金额：</span>
              <span class="value extra-amount">¥{{ (order.settlementAmount - order.totalPrice).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="section" v-if="order.status >= 2">
          <h3 class="section-title">取车信息</h3>
          <div class="pickup-info">
            <div class="info-item">
              <span class="label">取车时间：</span>
              <span class="value">{{ formatTime(order.pickupTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">取车人：</span>
              <span class="value">{{ order.pickupOperator || '-' }}</span>
            </div>
            <div class="info-item" v-if="order.pickupRemark">
              <span class="label">取车备注：</span>
              <span class="value">{{ order.pickupRemark }}</span>
            </div>
          </div>
        </div>

        <div class="section" v-if="order.status >= 3">
          <h3 class="section-title">还车信息</h3>
          <div class="return-info">
            <div class="info-item">
              <span class="label">还车时间：</span>
              <span class="value">{{ formatTime(order.returnTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">还车人：</span>
              <span class="value">{{ order.returnOperator || '-' }}</span>
            </div>
            <div class="info-item" v-if="order.returnRemark">
              <span class="label">还车备注：</span>
              <span class="value">{{ order.returnRemark }}</span>
            </div>
          </div>
        </div>

        <div class="section" v-if="order.orderLogs && order.orderLogs.length > 0">
          <h3 class="section-title">订单状态记录</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(log, index) in order.orderLogs"
              :key="index"
              :timestamp="formatTime(log.createTime)"
              placement="top"
            >
              <div class="timeline-content">
                <span class="status-change">{{ getStatusText(log.fromStatus) }} → {{ getStatusText(log.toStatus) }}</span>
                <span class="operator" v-if="log.operatorName">操作人: {{ log.operatorName }}</span>
                <span class="remark" v-if="log.remark">{{ log.remark }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="section">
          <h3 class="section-title">用户信息</h3>
          <div class="user-info">
            <div class="info-item">
              <span class="label">用户名：</span>
              <span class="value">{{ order.userName }}</span>
            </div>
            <div class="info-item">
              <span class="label">联系电话：</span>
              <span class="value">{{ order.userPhone }}</span>
            </div>
          </div>
        </div>

        <div class="section">
          <h3 class="section-title">订单信息</h3>
          <div class="order-info">
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ formatTime(order.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">更新时间：</span>
              <span class="value">{{ formatTime(order.updateTime) }}</span>
            </div>
          </div>
        </div>

        <div class="order-actions">
          <el-alert
            v-if="order.status === 2"
            title="订单已取车，请按时还车"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 16px;"
          />
          <el-button v-if="order.status === 0" type="primary" @click="handlePay">支付</el-button>
          <el-button v-if="order.status === 0" @click="handleCancel">取消</el-button>
          <el-button v-if="order.status === 3" @click="showReviewDialog">评价</el-button>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-empty description="订单不存在" />
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
        <el-button type="primary" @click="handleReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '@/api/order'
import { reviewApi } from '@/api/review'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const order = ref(null)
const reviewDialogVisible = ref(false)
const reviewForm = ref({
  rating: 5,
  content: ''
})

const defaultImage = 'https://via.placeholder.com/200x150?text=No+Image'

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger' }
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

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm')

const loadOrderDetail = async () => {
  const orderId = route.params.id
  if (!orderId) {
    ElMessage.error('订单ID不存在')
    router.push('/orders')
    return
  }

  loading.value = true
  try {
    const res = await orderApi.getDetail(orderId)
    order.value = res
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/orders')
}

const handlePay = async () => {
  try {
    await ElMessageBox.confirm(`确定要支付订单 ${order.value.orderNo} 吗？金额：¥${order.value.totalPrice}`, '确认支付', { type: 'info' })
    await orderApi.pay(order.value.id)
    ElMessage.success('支付成功')
    loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error('支付失败')
    }
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await orderApi.cancel(order.value.id)
    ElMessage.success('订单已取消')
    loadOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

const showReviewDialog = () => {
  reviewForm.value = {
    rating: 5,
    content: ''
  }
  reviewDialogVisible.value = true
}

const handleReview = async () => {
  if (!reviewForm.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  
  try {
    await reviewApi.create({
      orderId: order.value.id,
      carId: order.value.carId,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    loadOrderDetail()
  } catch (error) {
    console.error('评价失败:', error)
    ElMessage.error(error.message || '评价失败')
  }
}

onMounted(loadOrderDetail)
</script>

<style lang="scss" scoped>
.order-detail {
  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;

    .el-button {
      margin-right: 16px;
    }
  }

  .detail-content {
    min-height: 500px;
  }

  .detail-card {
    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 16px;
      border-bottom: 1px solid #ebeef5;
      margin-bottom: 20px;

      .order-no {
        font-size: 14px;
        color: #606266;
      }
    }

    .section {
      margin-bottom: 24px;

      .section-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 12px;
        color: #303133;
      }
    }

    .car-info {
      display: flex;
      gap: 20px;

      .car-image {
        width: 200px;
        height: 140px;
        border-radius: 4px;
      }

      .car-detail {
        flex: 1;

        h4 {
          font-size: 16px;
          margin-bottom: 8px;
        }

        p {
          margin-bottom: 4px;
          color: #606266;
          font-size: 14px;
        }
      }
    }

    .rent-info, .price-info, .user-info, .order-info {
      .info-item {
        display: flex;
        margin-bottom: 8px;

        .label {
          width: 120px;
          color: #909399;
          font-size: 14px;
        }

        .value {
          flex: 1;
          color: #303133;
          font-size: 14px;

          &.actual-time {
            color: #409eff;
            font-weight: 500;
          }

          &.highlight {
            color: #e6a23c;
            font-weight: 500;
          }
        }

        .compare-text {
          color: #909399;
          font-size: 12px;
          margin-left: 8px;
        }

        .store-info {
          flex: 1;

          .store-name {
            color: #303133;
            font-weight: 500;
            margin-bottom: 2px;
          }

          .store-address, .store-phone {
            color: #606266;
            font-size: 13px;
            margin-bottom: 2px;
          }
        }
      }
    }

    .price-info {
      .price-item {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;

        .label {
          color: #606266;
        }

        .value {
          font-weight: 500;
          color: #f56c6c;

          &.discount {
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
            margin-left: 8px;
          }
        }

        &.total {
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px dashed #dcdfe6;

          .label {
            font-weight: bold;
            color: #303133;
          }

          .value {
            font-size: 16px;
            font-weight: bold;
          }

          &.settlement {
            .value {
              color: #409eff;
            }
          }
        }

        &.refund {
          .label {
            color: #67c23a;
          }

          .refund-amount {
            color: #67c23a;
            font-weight: bold;
            font-size: 16px;
          }
        }

        &.extra {
          .label {
            color: #f56c6c;
          }

          .extra-amount {
            color: #f56c6c;
            font-weight: bold;
            font-size: 16px;
          }
        }
      }
    }

    .actual-fee-section {
      background: #f5f7fa;
      border-radius: 8px;
      padding: 16px;
    }

    .order-actions {
      margin-top: 24px;
      padding-top: 16px;
      border-top: 1px solid #ebeef5;
      text-align: right;
    }
    
    .timeline-content {
      .status-change {
        font-weight: 500;
        color: #409eff;
      }
      .operator, .remark {
        display: block;
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}
</style>
