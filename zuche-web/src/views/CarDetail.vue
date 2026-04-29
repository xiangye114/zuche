
<template>
  <div class="car-detail-page" v-loading="loading">
    <div class="container" v-if="car">
      <div class="detail-card">
        <div class="car-gallery">
          <el-image :src="car.image || defaultImage" fit="cover" class="main-image">
            <template #error>
              <div class="image-placeholder">
                <el-icon :size="48"><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="car-status-badge" :class="statusClass">{{ statusText }}</div>
        </div>

        <div class="car-content">
          <div class="car-header">
            <h1 class="car-name">{{ car.name }}</h1>
            <div class="car-tags">
              <el-tag v-if="car.brandName" effect="plain">{{ car.brandName }}</el-tag>
              <el-tag type="info" v-if="car.categoryName" effect="plain">{{ car.categoryName }}</el-tag>
            </div>
          </div>

          <div class="car-features">
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="feature-info">
                <span class="feature-label">座位数</span>
                <span class="feature-value">{{ car.seats }}座</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="feature-info">
                <span class="feature-label">变速箱</span>
                <span class="feature-value">{{ car.transmission }}</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Coin /></el-icon>
              </div>
              <div class="feature-info">
                <span class="feature-label">燃油类型</span>
                <span class="feature-value">{{ car.fuelType }}</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Wallet /></el-icon>
              </div>
              <div class="feature-info">
                <span class="feature-label">押金</span>
                <span class="feature-value">¥{{ car.deposit }}</span>
              </div>
            </div>
          </div>

          <div class="car-description">
            <h3>车辆描述</h3>
            <p>{{ car.description || '暂无描述' }}</p>
          </div>

          <div class="car-price-section">
            <div class="price-info">
              <span class="price-label">日租金</span>
              <div class="price-value">
                <span class="currency">¥</span>
                <span class="amount">{{ car.pricePerDay }}</span>
                <span class="unit">/天</span>
              </div>
            </div>
            <div class="price-actions">
              <el-button type="primary" size="large" @click="showBookingDialog" :disabled="car.status !== 1" class="book-btn">
                <el-icon><Calendar /></el-icon>
                立即预订
              </el-button>
              <el-button size="large" @click="toggleFavorite" class="favorite-btn">
                <el-icon><Star /></el-icon>
                {{ isFavorite ? '取消收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="container similar-cars-section" v-if="car && similarCars.length > 0">
      <div class="section-header">
        <h2>相似车型推荐</h2>
        <p>根据您查看的车型，为您推荐以下车辆</p>
      </div>
      <div class="similar-cars-scroll">
        <div class="similar-cars-list">
          <div 
            v-for="itemCar in similarCars" 
            :key="itemCar.id" 
            class="similar-car-item"
            @click="goToCarDetail(itemCar.id)"
          >
            <div class="car-image-wrapper">
              <el-image :src="itemCar.image || defaultImage" fit="cover" />
              <div class="car-status" :class="getStatusClass(itemCar.status)">
                {{ getStatusText(itemCar.status) }}
              </div>
            </div>
            <div class="car-info">
              <h4 class="car-name">{{ itemCar.name }}</h4>
              <div class="car-tags">
                <el-tag size="small" v-if="itemCar.brandName" effect="plain">{{ itemCar.brandName }}</el-tag>
                <el-tag size="small" type="info" v-if="itemCar.categoryName" effect="plain">{{ itemCar.categoryName }}</el-tag>
              </div>
              <div class="car-price">
                <span class="price-value">¥{{ itemCar.pricePerDay }}</span>
                <span class="price-unit">/天</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="bookingDialogVisible" title="预订车辆" width="520px" class="booking-dialog">
      <el-form ref="bookingFormRef" :model="bookingForm" :rules="bookingRules" label-width="100px" class="booking-form">
        <el-form-item label="取车时间" prop="startTime">
          <el-date-picker
            v-model="bookingForm.startTime"
            type="datetime"
            placeholder="选择取车时间"
            :disabled-date="disabledStartDate"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
            size="large"
          />
        </el-form-item>
        <el-form-item label="还车时间" prop="endTime">
          <el-date-picker
            v-model="bookingForm.endTime"
            type="datetime"
            placeholder="选择还车时间"
            :disabled-date="disabledEndDate"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
            size="large"
          />
        </el-form-item>
        <el-form-item label="取车门店" prop="pickupStoreId">
          <el-select v-model="bookingForm.pickupStoreId" placeholder="选择取车门店" style="width: 100%" size="large">
            <el-option v-for="store in stores" :key="store.id" :label="store.name" :value="store.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="还车门店" prop="returnStoreId">
          <el-select v-model="bookingForm.returnStoreId" placeholder="选择还车门店" style="width: 100%" size="large">
            <el-option v-for="store in stores" :key="store.id" :label="store.name" :value="store.id" />
          </el-select>
        </el-form-item>
        <div class="booking-summary">
          <div class="summary-item">
            <span class="summary-label">租期</span>
            <span class="summary-value">{{ rentDays }}天</span>
          </div>
          <div class="summary-item total">
            <span class="summary-label">租金总计</span>
            <span class="summary-value">¥{{ totalPrice }}</span>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="bookingDialogVisible = false" size="large">取消</el-button>
        <el-button type="primary" @click="showPayDialog" :loading="bookingLoading" size="large">确认预订</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="payDialogVisible" title="确认支付" width="480px" class="pay-dialog" :close-on-click-modal="false" :close-on-press-escape="false">
      <div class="pay-content">
        <div class="pay-info">
          <div class="info-item">
            <span class="label">订单金额</span>
            <span class="value">¥{{ totalPrice }}</span>
          </div>
          <div class="info-item">
            <span class="label">取车时间</span>
            <span class="value">{{ formatTime(bookingForm.startTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">还车时间</span>
            <span class="value">{{ formatTime(bookingForm.endTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">取车门店</span>
            <span class="value">{{ getStoreName(bookingForm.pickupStoreId) }}</span>
          </div>
          <div class="info-item">
            <span class="label">还车门店</span>
            <span class="value">{{ getStoreName(bookingForm.returnStoreId) }}</span>
          </div>
        </div>
        <div class="countdown" v-if="countdown > 0">
          <el-icon class="countdown-icon"><Timer /></el-icon>
          <span>支付剩余时间：{{ countdown }}秒</span>
        </div>
        <div class="countdown expired" v-else>
          <el-icon class="countdown-icon"><Warning /></el-icon>
          <span>支付已超时，请重新下单</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="handlePayCancel" size="large">取消</el-button>
        <el-button type="primary" @click="handlePayConfirm" :loading="payLoading" size="large" :disabled="countdown <= 0">确认支付</el-button>
      </template>
    </el-dialog>

    <div class="container reviews-section" v-if="car">
      <ReviewList ref="reviewListRef" :car-id="car.id" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { carApi } from '@/api/car'
import { orderApi } from '@/api/order'
import { recommendApi } from '@/api/recommend'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Picture, Calendar, Timer, Warning, Star, User, Cpu, Coin, Wallet } from '@element-plus/icons-vue'
import ReviewList from '@/components/ReviewList.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const car = ref(null)
const stores = ref([])
const isFavorite = ref(false)
const similarCars = ref([])
const defaultImage = 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=800&h=500&fit=crop'
const reviewListRef = ref(null)

const bookingDialogVisible = ref(false)
const bookingLoading = ref(false)
const bookingFormRef = ref(null)

const payDialogVisible = ref(false)
const payLoading = ref(false)
const countdown = ref(1800)
let countdownInterval = null
const currentOrderId = ref(null)

const bookingForm = reactive({
  startTime: null,
  endTime: null,
  pickupStoreId: null,
  returnStoreId: null
})

const statusClass = computed(() => {
  switch (car.value?.status) {
    case 0: return 'status-repair'
    case 1: return 'status-available'
    case 2: return 'status-rented'
    default: return ''
  }
})

const statusText = computed(() => {
  switch (car.value?.status) {
    case 0: return '维修中'
    case 1: return '可租'
    case 2: return '已租'
    default: return ''
  }
})

const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-repair'
    case 1: return 'status-available'
    case 2: return 'status-rented'
    default: return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '维修中'
    case 1: return '可租'
    case 2: return '已租'
    default: return ''
  }
}

const disabledStartDate = (time) => {
  const now = new Date()
  now.setHours(0, 0, 0, 0)
  return time.getTime() < now.getTime()
}

const disabledEndDate = (time) => {
  if (!bookingForm.startTime) {
    const now = new Date()
    now.setHours(0, 0, 0, 0)
    return time.getTime() < now.getTime()
  }
  const start = new Date(bookingForm.startTime)
  const minEnd = new Date(start.getTime() + 24 * 60 * 60 * 1000)
  return time.getTime() < minEnd.getTime()
}

const validateEndTime = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请选择还车时间'))
  } else if (!bookingForm.startTime) {
    callback(new Error('请先选择取车时间'))
  } else {
    const start = new Date(bookingForm.startTime)
    const end = new Date(value)
    const diffDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
    if (diffDays < 1) {
      callback(new Error('还车时间至少要比取车时间晚1天'))
    } else {
      callback()
    }
  }
}

const bookingRules = {
  startTime: [{ required: true, message: '请选择取车时间', trigger: 'change' }],
  endTime: [
    { required: true, message: '请选择还车时间', trigger: 'change' },
    { validator: validateEndTime, trigger: 'change' }
  ],
  pickupStoreId: [{ required: true, message: '请选择取车门店', trigger: 'change' }],
  returnStoreId: [{ required: true, message: '请选择还车门店', trigger: 'change' }]
}

const rentDays = computed(() => {
  if (!bookingForm.startTime || !bookingForm.endTime) return 0
  const diff = new Date(bookingForm.endTime) - new Date(bookingForm.startTime)
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
})

const totalPrice = computed(() => {
  if (!car.value || !rentDays.value) return 0
  return (car.value.pricePerDay * rentDays.value).toFixed(2)
})

const loadCar = async () => {
  loading.value = true
  try {
    const res = await carApi.getDetail(route.params.id)
    car.value = res
    if (userStore.isLoggedIn) {
      await loadFavoriteStatus()
    }
    await loadSimilarCars()
  } finally {
    loading.value = false
  }
}

const loadSimilarCars = async () => {
  try {
    const res = await recommendApi.getSimilarCars(route.params.id, 6)
    similarCars.value = res || []
  } catch (error) {
    console.error('加载相似车型失败:', error)
    similarCars.value = []
  }
}

const goToCarDetail = (carId) => {
  router.push({ path: `/cars/${carId}` })
}

const loadFavoriteStatus = async () => {
  try {
    const res = await carApi.isFavorite(route.params.id)
    isFavorite.value = res
  } catch (error) {
    console.error('加载收藏状态失败:', error)
    isFavorite.value = false
  }
}

const loadStores = async () => {
  try {
    const res = await carApi.getStores()
    stores.value = res || []
  } catch (error) {
    console.error('加载门店失败:', error)
    stores.value = []
  }
}

const getStoreName = (storeId) => {
  if (!storeId) return '未知门店'
  const store = stores.value.find(s => s.id === storeId)
  return store ? store.name : '未知门店'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const startCountdown = () => {
  countdown.value = 1800
  countdownInterval = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      clearInterval(countdownInterval)
    }
  }, 1000)
}

const stopCountdown = () => {
  if (countdownInterval) {
    clearInterval(countdownInterval)
    countdownInterval = null
  }
}

const showPayDialog = async () => {
  const valid = await bookingFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  bookingLoading.value = true
  try {
    const formatDate = (date) => {
      if (!date) return null
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hours = String(d.getHours()).padStart(2, '0')
      const minutes = String(d.getMinutes()).padStart(2, '0')
      const seconds = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
    }
    
    const orderRes = await orderApi.create({
      carId: car.value.id,
      startTime: formatDate(bookingForm.startTime),
      endTime: formatDate(bookingForm.endTime),
      pickupStoreId: bookingForm.pickupStoreId,
      returnStoreId: bookingForm.returnStoreId
    })
    
    if (orderRes && orderRes.id) {
      currentOrderId.value = orderRes.id
      bookingDialogVisible.value = false
      payDialogVisible.value = true
      startCountdown()
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error(error.message || '创建订单失败')
  } finally {
    bookingLoading.value = false
  }
}

const handlePayConfirm = async () => {
  if (!currentOrderId.value) {
    ElMessage.error('订单不存在')
    return
  }
  
  payLoading.value = true
  try {
    await orderApi.pay(currentOrderId.value)
    ElMessage.success('支付成功')
    stopCountdown()
    payDialogVisible.value = false
    currentOrderId.value = null
    router.push('/orders')
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.message || '支付失败')
  } finally {
    payLoading.value = false
  }
}

const handlePayCancel = () => {
  stopCountdown()
  payDialogVisible.value = false
  currentOrderId.value = null
  ElMessage.info('订单已创建，请尽快完成支付')
  router.push('/orders')
}

onUnmounted(() => {
  stopCountdown()
})

const showBookingDialog = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  
  const query = route.query
  if (query.pickupTime) bookingForm.startTime = query.pickupTime
  if (query.returnTime) bookingForm.endTime = query.returnTime
  if (query.pickupStoreId) bookingForm.pickupStoreId = Number(query.pickupStoreId)
  if (query.returnStoreId) bookingForm.returnStoreId = Number(query.returnStoreId)
  
  loadStores()
  bookingDialogVisible.value = true
}


const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isFavorite.value) {
      await carApi.removeFavorite(car.value.id)
      isFavorite.value = false
      ElMessage.success('已取消收藏')
    } else {
      await carApi.addFavorite(car.value.id)
      isFavorite.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadCar()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.car-detail-page {
  padding: 32px 0;
  min-height: calc(100vh - 64px);
}

.detail-card {
  display: flex;
  gap: 48px;
  background: $bg-primary;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: $shadow-card;
  
  @media (max-width: 1024px) {
    flex-direction: column;
    gap: 0;
  }
  
  .car-gallery {
    width: 55%;
    position: relative;
    flex-shrink: 0;
    
    @media (max-width: 1024px) {
      width: 100%;
    }
    
    .main-image {
      width: 100%;
      height: 420px;
      display: block;
      
      @media (max-width: 1024px) {
        height: 300px;
      }
    }
    
    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: $bg-tertiary;
      color: $text-disabled;
    }
    
    .car-status-badge {
      position: absolute;
      top: 20px;
      left: 20px;
      padding: 8px 20px;
      border-radius: 20px;
      font-size: 14px;
      font-weight: 600;
      color: #fff;
      backdrop-filter: blur(8px);
      
      &.status-available {
        background: linear-gradient(135deg, $success-color, $success-light);
      }
      
      &.status-rented {
        background: linear-gradient(135deg, $warning-color, $warning-light);
      }
      
      &.status-repair {
        background: $info-color;
      }
    }
  }
  
  .car-content {
    flex: 1;
    padding: 32px;
    display: flex;
    flex-direction: column;
    
    @media (max-width: 1024px) {
      padding: 24px;
    }
    
    .car-header {
      margin-bottom: 24px;
      
      .car-name {
        font-size: 32px;
        font-weight: 700;
        color: $text-primary;
        margin-bottom: 12px;
        letter-spacing: -0.02em;
      }
      
      .car-tags {
        display: flex;
        gap: 8px;
        
        :deep(.el-tag) {
          border-radius: 8px;
          padding: 4px 12px;
          font-size: 13px;
        }
      }
    }
    
    .car-features {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      margin-bottom: 24px;
      
      .feature-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        background: $bg-tertiary;
        border-radius: 12px;
        
        .feature-icon {
          width: 44px;
          height: 44px;
          background: rgba($primary-color, 0.1);
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: $primary-color;
          font-size: 20px;
        }
        
        .feature-info {
          display: flex;
          flex-direction: column;
          gap: 2px;
          
          .feature-label {
            font-size: 12px;
            color: $text-tertiary;
          }
          
          .feature-value {
            font-size: 15px;
            font-weight: 600;
            color: $text-primary;
          }
        }
      }
    }
    
    .car-description {
      flex: 1;
      margin-bottom: 24px;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: 12px;
      }
      
      p {
        color: $text-secondary;
        line-height: 1.7;
        font-size: 14px;
      }
    }
    
    .car-price-section {
      padding-top: 24px;
      border-top: 1px solid $border-light;
      
      .price-info {
        margin-bottom: 20px;
        
        .price-label {
          font-size: 14px;
          color: $text-tertiary;
          margin-bottom: 4px;
          display: block;
        }
        
        .price-value {
          display: flex;
          align-items: baseline;
          
          .currency {
            font-size: 20px;
            color: $danger-color;
            font-weight: 500;
          }
          
          .amount {
            font-size: 42px;
            font-weight: 700;
            color: $danger-color;
            line-height: 1;
          }
          
          .unit {
            font-size: 16px;
            color: $text-tertiary;
            margin-left: 4px;
          }
        }
      }
      
      .price-actions {
        display: flex;
        gap: 12px;
        
        .book-btn {
          flex: 1;
          height: 48px;
          font-size: 16px;
          font-weight: 600;
          border-radius: 12px;
          background: linear-gradient(135deg, $primary-color, $primary-light);
          border: none;
          box-shadow: 0 4px 14px rgba($primary-color, 0.35);
          
          &:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba($primary-color, 0.45);
          }
        }
        
        .favorite-btn {
          height: 48px;
          padding: 0 24px;
          border-radius: 12px;
          font-weight: 500;
        }
      }
    }
  }
}

.similar-cars-section {
  margin-top: 32px;
  padding: 32px;
  background: $bg-primary;
  border-radius: 20px;
  box-shadow: $shadow-card;
  
  .section-header {
    margin-bottom: 24px;
    
    h2 {
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
  
  .similar-cars-scroll {
    overflow-x: auto;
    padding-bottom: 16px;
    
    &::-webkit-scrollbar {
      height: 8px;
    }
    
    &::-webkit-scrollbar-track {
      background: $bg-tertiary;
      border-radius: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: $border-color;
      border-radius: 4px;
      
      &:hover {
        background: $text-disabled;
      }
    }
  }
  
  .similar-cars-list {
    display: flex;
    gap: 20px;
    padding: 8px 0;
    min-width: max-content;
  }
  
  .similar-car-item {
    width: 240px;
    flex-shrink: 0;
    background: $bg-tertiary;
    border-radius: 12px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-6px);
      box-shadow: $shadow-card-hover;
    }
    
    .car-image-wrapper {
      position: relative;
      height: 140px;
      
      .el-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .car-status {
        position: absolute;
        top: 8px;
        right: 8px;
        padding: 4px 10px;
        border-radius: 12px;
        font-size: 11px;
        font-weight: 600;
        color: #fff;
        backdrop-filter: blur(8px);
        
        &.status-available {
          background: linear-gradient(135deg, $success-color, $success-light);
        }
        
        &.status-rented {
          background: linear-gradient(135deg, $warning-color, $warning-light);
        }
        
        &.status-repair {
          background: $info-color;
        }
      }
    }
    
    .car-info {
      padding: 16px;
      
      .car-name {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .car-tags {
        display: flex;
        gap: 6px;
        margin-bottom: 12px;
        flex-wrap: wrap;
        
        :deep(.el-tag) {
          border-radius: 4px;
          padding: 2px 8px;
          font-size: 11px;
        }
      }
      
      .car-price {
        display: flex;
        align-items: baseline;
        gap: 2px;
        
        .price-value {
          font-size: 20px;
          font-weight: 700;
          color: $danger-color;
        }
        
        .price-unit {
          font-size: 12px;
          color: $text-tertiary;
        }
      }
    }
  }
}

.booking-dialog {
  .booking-form {
    padding: 20px 0;
  }
  
  .booking-summary {
    background: $bg-tertiary;
    border-radius: 12px;
    padding: 16px 20px;
    margin-top: 16px;
    
    .summary-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      
      .summary-label {
        color: $text-secondary;
        font-size: 14px;
      }
      
      .summary-value {
        font-weight: 600;
        color: $text-primary;
      }
      
      &.total {
        border-top: 1px solid $border-color;
        margin-top: 8px;
        padding-top: 16px;
        
        .summary-value {
          font-size: 24px;
          color: $danger-color;
        }
      }
    }
  }
}

.pay-dialog {
  .pay-content {
    padding: 20px 0;
    
    .pay-info {
      background: $bg-tertiary;
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 20px;
      
      .info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 0;
        
        .label {
          color: $text-secondary;
          font-size: 14px;
        }
        
        .value {
          font-weight: 600;
          color: $text-primary;
        }
      }
    }
    
    .countdown {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 16px;
      background: rgba($warning-color, 0.1);
      border-radius: 8px;
      margin-top: 16px;
      
      .countdown-icon {
        color: $warning-color;
      }
      
      &.expired {
        background: rgba($danger-color, 0.1);
        
        .countdown-icon {
          color: $danger-color;
        }
      }
    }
  }
}

.reviews-section {
  margin-top: 32px;
  padding: 32px;
  background: $bg-primary;
  border-radius: 20px;
  box-shadow: $shadow-card;
}
</style>
