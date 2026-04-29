<template>
  <div class="car-list-page">
    <div class="page-hero">
      <div class="container">
        <h1>车辆列表</h1>
        <p>选择您心仪的车型，开启美好旅程</p>
      </div>
    </div>
    
    <div class="container">
      <div class="search-bar">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <div class="form-grid">
            <el-form-item label="取车门店">
              <el-select v-model="searchForm.pickupStoreId" placeholder="选择取车门店" clearable filterable @change="handleStoreChange" size="large">
                <el-option v-for="store in stores" :key="store.id" :label="store.name" :value="store.id">
                  <span>{{ store.name }}</span>
                  <span class="option-address">{{ store.address }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="取车时间">
              <el-date-picker
                v-model="searchForm.pickupTime"
                type="datetime"
                placeholder="选择取车时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled-date="disabledDate"
                size="large"
              />
            </el-form-item>
            <el-form-item label="还车时间">
              <el-date-picker
                v-model="searchForm.returnTime"
                type="datetime"
                placeholder="选择还车时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled-date="disabledDate"
                size="large"
              />
            </el-form-item>
            <el-form-item label="还车门店">
              <el-select v-model="searchForm.returnStoreId" placeholder="选择还车门店" clearable filterable size="large">
                <el-option v-for="store in stores" :key="store.id" :label="store.name" :value="store.id">
                  <span>{{ store.name }}</span>
                  <span class="option-address">{{ store.address }}</span>
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="关键词">
              <el-input v-model="searchForm.keyword" placeholder="搜索车辆名称" clearable size="large" />
            </el-form-item>
            <el-form-item label="品牌">
              <el-select v-model="searchForm.brandId" placeholder="选择品牌" clearable size="large">
                <el-option v-for="brand in brands" :key="brand.id" :label="brand.name" :value="brand.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="searchForm.categoryId" placeholder="选择分类" clearable size="large">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="价格">
              <el-select v-model="searchForm.priceRange" placeholder="价格区间" clearable size="large">
                <el-option label="100以下" value="0-100" />
                <el-option label="100-200" value="100-200" />
                <el-option label="200-300" value="200-300" />
                <el-option label="300以上" value="300-" />
              </el-select>
            </el-form-item>
          </div>
          <div class="form-actions">
            <el-button type="primary" size="large" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button size="large" @click="handleReset">重置</el-button>
          </div>
        </el-form>
      </div>
      
      <div class="search-tips" v-if="searchForm.pickupTime && searchForm.returnTime">
        <el-alert
          :title="`已选择: 取车门店 ${getStoreName(searchForm.pickupStoreId)} | 取车时间 ${formatDate(searchForm.pickupTime)} | 还车时间 ${formatDate(searchForm.returnTime)}`"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
      
      <div class="results-section">
        <div class="results-header">
          <span class="results-count">共 {{ pagination.total }} 辆车</span>
        </div>
        
        <div class="car-grid" v-loading="loading">
          <CarCard v-for="car in cars" :key="car.id" :car="car" :pickup-time="searchForm.pickupTime" :return-time="searchForm.returnTime" :pickup-store-id="searchForm.pickupStoreId" :return-store-id="searchForm.returnStoreId" />
        </div>
        
        <el-empty v-if="!loading && cars.length === 0" description="暂无符合条件的车辆" />
        
        <div class="pagination" v-if="cars.length > 0">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[8, 16, 24, 32]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadCars"
            @current-change="loadCars"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { carApi } from '@/api/car'
import CarCard from '@/components/CarCard.vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const cars = ref([])
const brands = ref([])
const categories = ref([])
const stores = ref([])

const searchForm = reactive({
  pickupStoreId: null,
  returnStoreId: null,
  pickupTime: null,
  returnTime: null,
  keyword: '',
  brandId: null,
  categoryId: null,
  priceRange: ''
})

const pagination = reactive({
  current: 1,
  size: 8,
  total: 0
})

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const handleStoreChange = () => {
  if (!searchForm.returnStoreId) {
    searchForm.returnStoreId = searchForm.pickupStoreId
  }
}

const getStoreName = (storeId) => {
  if (!storeId) return '任意门店'
  const store = stores.value.find(s => s.id === storeId)
  return store ? store.name : '任意门店'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadStores = async () => {
  try {
    const res = await carApi.getStores()
    stores.value = res || []
  } catch (error) {
    console.error(error)
  }
}

const loadBrands = async () => {
  try {
    const res = await carApi.getBrands()
    brands.value = res || []
  } catch (error) {
    console.error(error)
  }
}

const loadCategories = async () => {
  try {
    const res = await carApi.getCategories()
    categories.value = res || []
  } catch (error) {
    console.error(error)
  }
}

const loadCars = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.current,
      size: pagination.size,
      pickupStoreId: searchForm.pickupStoreId,
      returnStoreId: searchForm.returnStoreId,
      pickupTime: searchForm.pickupTime,
      returnTime: searchForm.returnTime,
      keyword: searchForm.keyword,
      brandId: searchForm.brandId,
      categoryId: searchForm.categoryId,
      priceRange: searchForm.priceRange
    }
    const res = await carApi.getList(params)
    cars.value = res?.records || []
    pagination.total = res?.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchForm.pickupTime && searchForm.returnTime) {
    const pickup = new Date(searchForm.pickupTime)
    const returnTime = new Date(searchForm.returnTime)
    if (returnTime <= pickup) {
      ElMessage.warning('还车时间必须晚于取车时间')
      return
    }
  }
  pagination.current = 1
  loadCars()
}

const handleReset = () => {
  searchForm.pickupStoreId = null
  searchForm.returnStoreId = null
  searchForm.pickupTime = null
  searchForm.returnTime = null
  searchForm.keyword = ''
  searchForm.brandId = null
  searchForm.categoryId = null
  searchForm.priceRange = ''
  pagination.current = 1
  loadCars()
}

onMounted(() => {
  handleQueryParams()
  loadStores()
  loadBrands()
  loadCategories()
  loadCars()
})

const handleQueryParams = () => {
  const query = router.currentRoute.value.query
  if (query.pickupStoreId) searchForm.pickupStoreId = Number(query.pickupStoreId)
  if (query.returnStoreId) searchForm.returnStoreId = Number(query.returnStoreId)
  if (query.pickupTime) searchForm.pickupTime = query.pickupTime
  if (query.returnTime) searchForm.returnTime = query.returnTime
}

watch(() => router.currentRoute.value.query, () => {
  handleQueryParams()
  loadCars()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.car-list-page {
  min-height: 100%;
}

.page-hero {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 50%, #0891b2 100%);
  padding: 48px 0;
  color: #fff;
  margin-bottom: 32px;
  
  h1 {
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 16px;
    opacity: 0.9;
  }
}

.search-bar {
  background: $bg-primary;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: $shadow-card;
  
  .search-form {
    .form-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 16px;
      margin-bottom: 20px;
      
      @media (max-width: 1200px) {
        grid-template-columns: repeat(2, 1fr);
      }
      
      @media (max-width: 640px) {
        grid-template-columns: 1fr;
      }
    }
    
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
    
    :deep(.el-form-item__label) {
      font-weight: 500;
      color: $text-secondary;
      font-size: 13px;
      padding-bottom: 6px;
    }
    
    :deep(.el-input__wrapper),
    :deep(.el-select__wrapper) {
      border-radius: 10px;
    }
    
    .option-address {
      float: right;
      color: $text-tertiary;
      font-size: 12px;
    }
    
    .form-actions {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
      
      .el-button {
        border-radius: 10px;
        font-weight: 500;
        min-width: 100px;
      }
    }
  }
}

.search-tips {
  margin-bottom: 24px;
  
  :deep(.el-alert) {
    border-radius: 12px;
  }
}

.results-section {
  .results-header {
    margin-bottom: 20px;
    
    .results-count {
      font-size: 15px;
      color: $text-secondary;
      font-weight: 500;
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
  
  .pagination {
    margin-top: 32px;
    display: flex;
    justify-content: center;
  }
}
</style>
