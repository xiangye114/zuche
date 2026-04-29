
<template>
  <div class="home-page">
    <section class="hero">
      <div class="hero-bg">
        <div class="bg-gradient"></div>
        <div class="bg-pattern"></div>
      </div>
      <div class="container hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            <span class="title-line">在线租车</span>
            <span class="title-line highlight">轻松出行</span>
          </h1>
          <p class="hero-subtitle">多种车型任您选择，专业服务保障您的旅程</p>
        </div>
        
        <div class="search-box">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <div class="form-row">
              <el-form-item label="取车时间" class="time-item">
                <el-date-picker
                  v-model="searchForm.pickupTime"
                  type="datetime"
                  placeholder="选择取车时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  :disabled-date="disabledDate"
                  :shortcuts="timeShortcuts"
                  size="large"
                />
              </el-form-item>
              <el-form-item label="还车时间" class="time-item">
                <el-date-picker
                  v-model="searchForm.returnTime"
                  type="datetime"
                  placeholder="选择还车时间"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  :disabled-date="disabledDate"
                  :shortcuts="timeShortcuts"
                  size="large"
                />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="取车网点" class="store-item">
                <el-select
                  v-model="searchForm.pickupStoreId"
                  placeholder="选择取车网点"
                  filterable
                  clearable
                  size="large"
                  @change="handlePickupStoreChange"
                >
                  <el-option
                    v-for="store in stores"
                    :key="store.id"
                    :label="store.name"
                    :value="store.id"
                  >
                    <div class="store-option">
                      <span class="store-name">{{ store.name }}</span>
                      <span class="store-address">{{ store.address }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="还车网点" class="store-item">
                <el-select
                  v-model="searchForm.returnStoreId"
                  placeholder="选择还车网点"
                  filterable
                  clearable
                  size="large"
                >
                  <el-option
                    v-for="store in returnStoreOptions"
                    :key="store.id"
                    :label="store.name"
                    :value="store.id"
                  >
                    <div class="store-option">
                      <span class="store-name">{{ store.name }}</span>
                      <span class="store-address">{{ store.address }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
            </div>
            <el-form-item class="search-btn-item">
              <el-button type="primary" size="large" class="search-btn" @click="handleSearch" :disabled="!canSearch">
                <el-icon><Search /></el-icon>
                <span>立即租车</span>
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="search-tips" v-if="!canSearch">
            <el-icon><InfoFilled /></el-icon>
            <span>请先选择取车时间、还车时间和取车网点</span>
          </div>
        </div>
      </div>
    </section>
    
    <section class="features container">
      <div class="section-header">
        <h2>为什么选择我们</h2>
        <p>专业、便捷、可靠的租车服务</p>
      </div>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon :size="32"><Van /></el-icon>
          </div>
          <h3>车型丰富</h3>
          <p>经济型、舒适型、SUV、商务车等多种车型，满足您的不同需求</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon :size="32"><Wallet /></el-icon>
          </div>
          <h3>价格透明</h3>
          <p>无隐藏费用，明码标价，让您租得放心、用得安心</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon :size="32" component="Shield" />
          </div>
          <h3>安全保障</h3>
          <p>全面保险覆盖，24小时道路救援，保障您的出行安全</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">
            <el-icon :size="32" component="Headphones" />
          </div>
          <h3>专业服务</h3>
          <p>24小时客服在线，随时为您解答疑问，提供贴心服务</p>
        </div>
      </div>
    </section>
    
    <section class="personalized-recommend container" v-if="isLoggedIn && recommendCars.length > 0">
      <div class="section-header">
        <div class="header-left">
          <div class="recommend-badge">
            <el-icon :size="16" component="Sparkles" />
            <span>为你推荐</span>
          </div>
          <h2>根据您的偏好推荐</h2>
          <p>基于您的订单历史和收藏记录</p>
        </div>
        <el-button type="primary" plain @click="goToCarList" class="more-btn">
          查看更多
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      
      <div class="car-grid" v-loading="recommendLoading">
        <CarCard v-for="car in recommendCars" :key="car.id" :car="car" />
      </div>
    </section>
    
    <section class="recommend container">
      <div class="section-header">
        <div class="header-left">
          <h2>热门推荐</h2>
          <p>精选优质车型，为您的出行保驾护航</p>
        </div>
        <el-button type="primary" plain @click="goToCarList" class="more-btn">
          查看更多
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      
      <div class="car-grid" v-loading="hotLoading">
        <CarCard v-for="car in hotCars" :key="car.id" :car="car" />
      </div>
    </section>
    
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2>开始您的租车之旅</h2>
          <p>注册即可享受专属优惠，开启便捷出行体验</p>
          <el-button type="primary" size="large" class="cta-btn" @click="$router.push('/register')">
            立即注册
          </el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { carApi } from '@/api/car'
import { storeApi } from '@/api/store'
import { recommendApi } from '@/api/recommend'
import { useUserStore } from '@/store/user'
import CarCard from '@/components/CarCard.vue'
import { InfoFilled, Search, Wallet, ArrowRight, Van } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const hotLoading = ref(false)
const recommendLoading = ref(false)
const hotCars = ref([])
const recommendCars = ref([])
const stores = ref([])

const isLoggedIn = computed(() => {
  return userStore.token && userStore.token !== ''
})

const searchForm = ref({
  pickupTime: '',
  returnTime: '',
  pickupStoreId: null,
  returnStoreId: null
})

const canSearch = computed(() => {
  return searchForm.value.pickupTime && 
         searchForm.value.returnTime && 
         searchForm.value.pickupStoreId
})

const returnStoreOptions = computed(() => {
  if (!searchForm.value.pickupStoreId) return stores.value
  return stores.value
})

const timeShortcuts = [
  {
    text: '今天',
    value: new Date()
  },
  {
    text: '明天',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 1)
      return date
    }
  },
  {
    text: '后天',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 2)
      return date
    }
  }
]

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const handlePickupStoreChange = (storeId) => {
  if (!searchForm.value.returnStoreId) {
    searchForm.value.returnStoreId = storeId
  }
}

const handleSearch = () => {
  if (!canSearch.value) return
  
  router.push({
    path: '/cars',
    query: {
      pickupTime: searchForm.value.pickupTime,
      returnTime: searchForm.value.returnTime,
      pickupStoreId: searchForm.value.pickupStoreId,
      returnStoreId: searchForm.value.returnStoreId || searchForm.value.pickupStoreId
    }
  })
}

const goToCarList = () => {
  router.push('/cars')
}

const loadStores = async () => {
  try {
    const res = await storeApi.getList()
    stores.value = res || []
  } catch (error) {
    console.error('加载门店列表失败:', error)
  }
}

const loadRecommendCars = async () => {
  if (!isLoggedIn.value) return
  recommendLoading.value = true
  try {
    const res = await recommendApi.getRecommendForUser(6)
    recommendCars.value = res || []
  } catch (error) {
    console.error('加载推荐车辆失败:', error)
    recommendCars.value = []
  } finally {
    recommendLoading.value = false
  }
}

const loadHotCars = async () => {
  hotLoading.value = true
  try {
    const res = await carApi.getList({ page: 1, size: 6, sort: 'hot' })
    hotCars.value = res?.records || []
  } catch (error) {
    console.error('加载热门车辆失败:', error)
    hotCars.value = []
  } finally {
    hotLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([
    loadStores(),
    loadRecommendCars(),
    loadHotCars()
  ])
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.home-page {
  background: $bg-secondary;
}

.hero {
  position: relative;
  padding: 80px 0 100px;
  overflow: hidden;
  
  .hero-bg {
    position: absolute;
    inset: 0;
    
    .bg-gradient {
      position: absolute;
      inset: 0;
      background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 40%, #0891b2 100%);
    }
    
    .bg-pattern {
      position: absolute;
      inset: 0;
      background-image: 
        radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
        radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.08) 0%, transparent 40%);
    }
  }
  
  .hero-content {
    position: relative;
    z-index: 1;
  }
  
  .hero-text {
    text-align: center;
    margin-bottom: 48px;
    
    .hero-title {
      color: #fff;
      margin-bottom: 16px;
      
      .title-line {
        display: block;
        font-size: 48px;
        font-weight: 700;
        letter-spacing: -0.02em;
        line-height: 1.2;
        
        @media (max-width: 768px) {
          font-size: 36px;
        }
      }
      
      .title-line.highlight {
        background: linear-gradient(90deg, #60a5fa, #34d399);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }
    
    .hero-subtitle {
      font-size: 18px;
      color: rgba(255, 255, 255, 0.85);
      
      @media (max-width: 768px) {
        font-size: 16px;
      }
    }
  }
  
  .search-box {
    background: rgba(255, 255, 255, 0.98);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    padding: 32px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
    max-width: 900px;
    margin: 0 auto;
    
    @media (max-width: 768px) {
      padding: 24px 20px;
      border-radius: 16px;
    }
    
    .search-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      
      .form-row {
        display: flex;
        gap: 16px;
        flex-wrap: wrap;
        
        @media (max-width: 768px) {
          flex-direction: column;
        }
      }
      
      :deep(.el-form-item) {
        margin-bottom: 0;
        flex: 1;
        min-width: 200px;
      }
      
      :deep(.el-form-item__label) {
        color: $text-secondary;
        font-weight: 500;
        font-size: 13px;
        padding-bottom: 6px;
      }
      
      :deep(.el-input__wrapper),
      :deep(.el-select__wrapper) {
        border-radius: 12px;
        box-shadow: 0 0 0 1px $border-color inset;
        transition: all 0.2s ease;
        
        &:hover {
          box-shadow: 0 0 0 1px $primary-light inset;
        }
        
        &.is-focus {
          box-shadow: 0 0 0 2px rgba($primary-color, 0.15), 0 0 0 1px $primary-color inset;
        }
      }
      
      .search-btn-item {
        width: 100%;
        margin-top: 8px;
        
        :deep(.el-form-item__content) {
          justify-content: center;
        }
        
        @media (max-width: 768px) {
          width: 100%;
          
          :deep(.el-form-item__content) {
            width: 100%;
            justify-content: center;
          }
        }
      }
      
      .search-btn {
        min-width: 160px;
        height: 44px;
        font-size: 16px;
        font-weight: 600;
        border-radius: 12px;
        background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
        border: none;
        box-shadow: 0 4px 14px rgba($primary-color, 0.35);
        transition: all 0.3s ease;
        
        &:hover:not(:disabled) {
          transform: translateY(-2px);
          box-shadow: 0 8px 24px rgba($primary-color, 0.45);
        }
        
        &:disabled {
          background: $text-disabled;
          box-shadow: none;
        }
      }
    }
    
    .search-tips {
      margin-top: 16px;
      padding: 12px 16px;
      background: rgba($primary-color, 0.08);
      border-radius: 10px;
      color: $text-tertiary;
      font-size: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      
      .el-icon {
        color: $primary-color;
      }
    }
  }
  
  .store-option {
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    .store-name {
      color: $text-primary;
      font-weight: 500;
    }
    
    .store-address {
      color: $text-tertiary;
      font-size: 12px;
    }
  }
}

.features {
  padding: 80px 0;
  
  .section-header {
    text-align: center;
    margin-bottom: 48px;
    
    h2 {
      font-size: 32px;
      font-weight: 700;
      color: $text-primary;
      margin-bottom: 12px;
    }
    
    p {
      color: $text-tertiary;
      font-size: 16px;
    }
  }
  
  .features-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
    
    @media (max-width: 1024px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
  }
  
  .feature-card {
    background: $bg-primary;
    border-radius: 16px;
    padding: 32px 24px;
    text-align: center;
    box-shadow: $shadow-card;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-8px);
      box-shadow: $shadow-card-hover;
    }
    
    .feature-icon {
      width: 64px;
      height: 64px;
      margin: 0 auto 20px;
      background: linear-gradient(135deg, rgba($primary-color, 0.1) 0%, rgba($primary-light, 0.15) 100%);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: $primary-color;
    }
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 12px;
    }
    
    p {
      color: $text-tertiary;
      font-size: 14px;
      line-height: 1.6;
    }
  }
}

.personalized-recommend {
  padding: 60px 0;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 32px;
    
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .header-left {
      .recommend-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 6px 12px;
        background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
        color: #92400e;
        margin-bottom: 12px;
        
        .el-icon {
          color: #f59e0b;
        }
      }
      
      h2 {
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
    
    .more-btn {
      border-radius: 10px;
      font-weight: 500;
      
      .el-icon {
        margin-left: 4px;
      }
    }
  }
  
  .car-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
    min-height: 300px;
    
    @media (max-width: 1200px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
  }
}

.recommend {
  padding-bottom: 80px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 32px;
    
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .header-left {
      h2 {
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
    
    .more-btn {
      border-radius: 10px;
      font-weight: 500;
      
      .el-icon {
        margin-left: 4px;
      }
    }
  }
  
  .car-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
    min-height: 300px;
    
    @media (max-width: 1200px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
  }
}

.cta-section {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 50%, #0891b2 100%);
  padding: 80px 0;
  
  .cta-content {
    text-align: center;
    
    h2 {
      font-size: 32px;
      font-weight: 700;
      color: #fff;
      margin-bottom: 12px;
    }
    
    p {
      color: rgba(255, 255, 255, 0.85);
      font-size: 16px;
      margin-bottom: 32px;
    }
    
    .cta-btn {
      height: 52px;
      padding: 0 40px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 12px;
      background: #fff;
      color: $primary-color;
      border: none;
      box-shadow: 0 4px 14px rgba(0, 0, 0, 0.15);
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
      }
    }
  }
}
</style>
