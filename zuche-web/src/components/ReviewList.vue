<template>
  <div class="review-list">
    <div class="review-header">
      <div class="header-left">
        <h3>用户评价</h3>
        <div class="rating-summary" v-if="avgRating > 0">
          <el-rate :value="avgRating" disabled allow-half />
          <span class="rating-value">{{ avgRating.toFixed(1) }}</span>
          <span class="review-count">({{ reviews.length }}条)</span>
        </div>
        <div class="rating-summary" v-else>
          <span class="no-review">暂无评价</span>
        </div>
      </div>
    </div>

    <div class="review-items">
      <div class="review-item" v-for="review in reviews" :key="review.id">
        <div class="review-header-item">
          <div class="user-info">
            <el-avatar :size="48" :src="review.userAvatar" class="user-avatar">
              {{ review.userName?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="user-detail">
              <span class="user-name">{{ review.userName || '匿名用户' }}</span>
              <span class="review-time">{{ formatTime(review.createTime) }}</span>
            </div>
          </div>
          <el-rate :value="review.rating" disabled class="review-rating" />
        </div>
        <div class="review-content">
          <p>{{ review.content || '暂无评价内容' }}</p>
        </div>
        <div class="review-meta">
          <span class="car-name">{{ review.carName }}</span>
        </div>
      </div>

      <div class="empty-review" v-if="reviews.length === 0">
        <el-empty description="暂无评价，成为第一个评价的人吧！" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  carId: {
    type: Number,
    required: true
  },
  autoLoad: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['reviewAdded'])

const reviews = ref([])
const avgRating = ref(0)
const loading = ref(false)

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadReviews = async () => {
  if (!props.carId) return
  loading.value = true
  try {
    const { reviewApi } = await import('@/api/review')
    const res = await reviewApi.getCarReviews(props.carId)
    reviews.value = res.reviews || []
    avgRating.value = res.avgRating || 0
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loading.value = false
  }
}

const refresh = () => {
  loadReviews()
}

watch(() => props.carId, () => {
  if (props.autoLoad) {
    loadReviews()
  }
}, { immediate: props.autoLoad })

defineExpose({ refresh })
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.review-list {
  .review-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    
    .header-left {
      h3 {
        font-size: 20px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: 12px;
      }
      
      .rating-summary {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .rating-value {
          font-size: 24px;
          font-weight: 700;
          color: $warning-color;
        }
        
        .review-count {
          color: $text-tertiary;
          font-size: 14px;
        }
        
        .no-review {
          color: $text-tertiary;
          font-size: 14px;
        }
      }
      
      :deep(.el-rate) {
        margin-right: 8px;
      }
    }
  }
  
  .review-items {
    display: flex;
    flex-direction: column;
    gap: 20px;
    
    .review-item {
      background: $bg-primary;
      border-radius: 16px;
      padding: 20px;
      box-shadow: $shadow-card;
      
      .review-header-item {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 16px;
        
        .user-info {
          display: flex;
          gap: 12px;
          
          .user-avatar {
            background: linear-gradient(135deg, $primary-color, $primary-light);
            color: #fff;
            font-weight: 600;
          }
          
          .user-detail {
            display: flex;
            flex-direction: column;
            gap: 4px;
            
            .user-name {
              font-weight: 600;
              color: $text-primary;
            }
            
            .review-time {
              font-size: 13px;
              color: $text-tertiary;
            }
          }
        }
        
        .review-rating {
          :deep(.el-rate__item) {
            font-size: 18px;
          }
        }
      }
      
      .review-content {
        p {
          color: $text-secondary;
          line-height: 1.7;
          font-size: 15px;
        }
      }
      
      .review-meta {
        margin-top: 12px;
        padding-top: 12px;
        border-top: 1px solid $border-light;
        
        .car-name {
          font-size: 13px;
          color: $text-tertiary;
        }
      }
    }
    
    .empty-review {
      padding: 40px 0;
    }
  }
}
</style>
