
<template>
  <div class="car-card" @click="handleClick">
    <div class="car-image">
      <el-image :src="car.image || defaultImage" fit="cover" />
      <div class="car-status" :class="statusClass">{{ statusText }}</div>
      <div class="image-overlay"></div>
    </div>
    <div class="car-info">
      <h3 class="car-name">{{ car.name }}</h3>
      <div class="car-tags">
        <el-tag size="small" v-if="car.brandName" effect="plain">{{ car.brandName }}</el-tag>
        <el-tag size="small" type="info" v-if="car.categoryName" effect="plain">{{ car.categoryName }}</el-tag>
      </div>
      
      <div class="recommend-tags" v-if="hasRecommendReasons">
        <span 
          v-for="(reason, index) in displayReasons" 
          :key="index" 
          class="recommend-tag"
          :class="getReasonClass(reason)"
        >
          <span class="tag-icon">{{ getReasonIcon(reason) }}</span>
          <span class="tag-text">{{ getReasonText(reason) }}</span>
        </span>
      </div>
      
      <div class="car-features">
        <span class="feature-item">
          <el-icon><User /></el-icon>
          {{ car.seats }}座
        </span>
        <span class="feature-item">
          <el-icon><Cpu /></el-icon>
          {{ car.transmission }}
        </span>
      </div>
      <div class="car-footer">
        <div class="car-price">
          <template v-if="totalPrice">
            <span class="price-value">¥{{ totalPrice }}</span>
            <span class="price-unit">/共{{ rentalDays }}天</span>
          </template>
          <template v-else>
            <span class="price-value">¥{{ car.pricePerDay }}</span>
            <span class="price-unit">/天</span>
          </template>
        </div>
        <div class="store-name" v-if="car.storeName">
          <el-icon><Location /></el-icon>
          {{ car.storeName }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Location, User, Cpu } from '@element-plus/icons-vue'

const router = useRouter()

const props = defineProps({
  car: {
    type: Object,
    required: true
  },
  pickupTime: {
    type: String,
    default: null
  },
  returnTime: {
    type: String,
    default: null
  },
  pickupStoreId: {
    type: [Number, String],
    default: null
  },
  returnStoreId: {
    type: [Number, String],
    default: null
  }
})

const defaultImage = 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=400&h=300&fit=crop'

const handleClick = () => {
  const query = {}
  if (props.pickupTime) query.pickupTime = props.pickupTime
  if (props.returnTime) query.returnTime = props.returnTime
  if (props.pickupStoreId) query.pickupStoreId = props.pickupStoreId
  if (props.returnStoreId) query.returnStoreId = props.returnStoreId
  
  router.push({
    path: `/cars/${props.car.id}`,
    query: Object.keys(query).length > 0 ? query : undefined
  })
}

const statusClass = computed(() => {
  switch (props.car.status) {
    case 0: return 'status-repair'
    case 1: return 'status-available'
    case 2: return 'status-rented'
    default: return ''
  }
})

const statusText = computed(() => {
  switch (props.car.status) {
    case 0: return '维修中'
    case 1: return '可租'
    case 2: return '已租'
    default: return ''
  }
})

const hasRecommendReasons = computed(() => {
  return props.car.recommendReasons && props.car.recommendReasons.length > 0
})

const displayReasons = computed(() => {
  if (!hasRecommendReasons.value) return []
  return props.car.recommendReasons.slice(0, 3)
})

const getReasonText = (reason) => {
  const reasonMap = {
    'BRAND_MATCH': '品牌匹配',
    'CATEGORY_MATCH': '类别匹配',
    'PRICE_MATCH': '价格合适',
    'HOT': '热门车型',
    'SIMILAR': '相似推荐'
  }
  return reasonMap[reason] || reason
}

const getReasonIcon = (reason) => {
  const iconMap = {
    'BRAND_MATCH': '🏷️',
    'CATEGORY_MATCH': '📊',
    'PRICE_MATCH': '💰',
    'HOT': '🔥',
    'SIMILAR': '🔗'
  }
  return iconMap[reason] || '✨'
}

const getReasonClass = (reason) => {
  const classMap = {
    'BRAND_MATCH': 'reason-brand',
    'CATEGORY_MATCH': 'reason-category',
    'PRICE_MATCH': 'reason-price',
    'HOT': 'reason-hot',
    'SIMILAR': 'reason-similar'
  }
  return classMap[reason] || ''
}

const totalPrice = computed(() => {
  if (!props.pickupTime || !props.returnTime || !props.car.pricePerDay) {
    return null
  }
  const pickup = new Date(props.pickupTime)
  const returnTime = new Date(props.returnTime)
  const days = Math.ceil((returnTime - pickup) / (1000 * 60 * 60 * 24))
  if (days < 1) return null
  return (days * props.car.pricePerDay).toFixed(2)
})

const rentalDays = computed(() => {
  if (!props.pickupTime || !props.returnTime) return null
  const pickup = new Date(props.pickupTime)
  const returnTime = new Date(props.returnTime)
  const days = Math.ceil((returnTime - pickup) / (1000 * 60 * 60 * 24))
  return days
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.car-card {
  background: $bg-primary;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: $shadow-card;
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: $shadow-card-hover;
    
    .car-image {
      .image-overlay {
        opacity: 0.1;
      }
    }
    
    .car-name {
      color: $primary-color;
    }
  }
  
  .car-image {
    position: relative;
    height: 180px;
    overflow: hidden;
    
    .el-image {
      width: 100%;
      height: 100%;
      transition: transform 0.5s ease;
    }
    
    &:hover .el-image {
      transform: scale(1.05);
    }
    
    .car-status {
      position: absolute;
      top: 12px;
      right: 12px;
      padding: 6px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      color: #fff;
      backdrop-filter: blur(8px);
      z-index: 2;
      
      &.status-available {
        background: linear-gradient(135deg, $success-color, $success-light);
        box-shadow: 0 2px 8px rgba($success-color, 0.3);
      }
      
      &.status-rented {
        background: linear-gradient(135deg, $warning-color, $warning-light);
        box-shadow: 0 2px 8px rgba($warning-color, 0.3);
      }
      
      &.status-repair {
        background: $info-color;
        box-shadow: 0 2px 8px rgba($info-color, 0.3);
      }
    }
    
    .image-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(180deg, transparent 50%, rgba(0, 0, 0, 0.3) 100%);
      opacity: 0;
      transition: opacity 0.3s ease;
      z-index: 1;
    }
  }
  
  .car-info {
    padding: 20px;
    
    .car-name {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
      margin-bottom: 12px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      transition: color 0.2s ease;
    }
    
    .car-tags {
      display: flex;
      gap: 8px;
      margin-bottom: 12px;
      flex-wrap: wrap;
      
      :deep(.el-tag) {
        border-radius: 6px;
        font-size: 12px;
        padding: 2px 8px;
      }
    }
    
    .recommend-tags {
      display: flex;
      gap: 8px;
      margin-bottom: 12px;
      flex-wrap: wrap;
      
      .recommend-tag {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: 500;
        animation: fadeInUp 0.3s ease-out;
        
        .tag-icon {
          font-size: 14px;
        }
        
        .tag-text {
          line-height: 1;
        }
        
        &.reason-brand {
          background: #dbeafe;
          color: #1d4ed8;
        }
        
        &.reason-category {
          background: #dcfce7;
          color: #166534;
        }
        
        &.reason-price {
          background: #fef9c3;
          color: #854d0e;
        }
        
        &.reason-hot {
          background: #fce7f3;
          color: #9d174d;
        }
        
        &.reason-similar {
          background: #e0e7ff;
          color: #4338ca;
        }
      }
    }
    
    .car-features {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
      
      .feature-item {
        display: flex;
        align-items: center;
        gap: 6px;
        color: $text-tertiary;
        font-size: 13px;
        
        .el-icon {
          font-size: 16px;
          color: $primary-light;
        }
      }
    }
    
    .car-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 16px;
      border-top: 1px solid $border-light;
      
      .car-price {
        display: flex;
        align-items: baseline;
        gap: 4px;
        
        .price-value {
          font-size: 24px;
          font-weight: 700;
          color: $danger-color;
        }
        
        .price-unit {
          color: $text-tertiary;
          font-size: 13px;
        }
      }
      
      .store-name {
        display: flex;
        align-items: center;
        gap: 4px;
        color: $text-tertiary;
        font-size: 12px;
        max-width: 120px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        
        .el-icon {
          font-size: 14px;
          flex-shrink: 0;
        }
      }
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
