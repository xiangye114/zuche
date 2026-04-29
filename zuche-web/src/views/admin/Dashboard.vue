<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h1>数据概览</h1>
      <p>欢迎回来，{{ userStore.user?.username }}</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon cars">
          <el-icon :size="28"><Van /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalCars }}</span>
          <span class="stat-label">车辆总数</span>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon orders">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalOrders }}</span>
          <span class="stat-label">订单总数</span>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon users">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.totalUsers }}</span>
          <span class="stat-label">用户总数</span>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon revenue">
          <el-icon :size="28"><Coin /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">¥{{ stats.totalRevenue }}</span>
          <span class="stat-label">总收入</span>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <div class="card recent-orders">
        <div class="card-header">
          <h3>最近订单</h3>
          <el-button text type="primary" @click="$router.push('/admin/orders')">
            查看全部
          </el-button>
        </div>
        <el-table :data="recentOrders" style="width: 100%" v-loading="ordersLoading">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="carName" label="车辆" min-width="120" />
          <el-table-column prop="userName" label="用户" width="100" />
          <el-table-column prop="totalAmount" label="金额" width="100">
            <template #default="{ row }">
              <span class="amount">¥{{ row.totalAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="card quick-actions">
        <div class="card-header">
          <h3>快捷操作</h3>
        </div>
        <div class="actions-grid">
          <div class="action-item" @click="$router.push('/admin/cars/create')">
            <div class="action-icon">
              <el-icon :size="24"><Plus /></el-icon>
            </div>
            <span>添加车辆</span>
          </div>
          <div class="action-item" @click="$router.push('/admin/orders')">
            <div class="action-icon">
              <el-icon :size="24"><Document /></el-icon>
            </div>
            <span>订单管理</span>
          </div>
          <div class="action-item" @click="$router.push('/admin/users')">
            <div class="action-icon">
              <el-icon :size="24"><User /></el-icon>
            </div>
            <span>用户管理</span>
          </div>
          <div class="action-item" @click="$router.push('/admin/stores')">
            <div class="action-icon">
              <el-icon :size="24"><OfficeBuilding /></el-icon>
            </div>
            <span>门店管理</span>
          </div>
        </div>
      </div>
    </div>

    <div class="card car-status">
      <div class="card-header">
        <h3>车辆状态统计</h3>
      </div>
      <div class="status-grid">
        <div class="status-item available">
          <div class="status-count">{{ carStats.available }}</div>
          <div class="status-label">可租车辆</div>
        </div>
        <div class="status-item rented">
          <div class="status-count">{{ carStats.rented }}</div>
          <div class="status-label">已租车辆</div>
        </div>
        <div class="status-item repair">
          <div class="status-count">{{ carStats.repair }}</div>
          <div class="status-label">维修中</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, onBeforeRouteUpdate } from 'vue-router'
import { useUserStore } from '@/store/user'
import { adminApi } from '@/api/admin'
import { Van, Document, User, Coin, Plus, OfficeBuilding } from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()
const ordersLoading = ref(false)

const stats = reactive({
  totalCars: 0,
  totalOrders: 0,
  totalUsers: 0,
  totalRevenue: '0.00'
})

const carStats = reactive({
  available: 0,
  rented: 0,
  repair: 0
})

const recentOrders = ref([])

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '待支付', 1: '已支付', 2: '已完成', 3: '已取消' }
  return texts[status] || '未知'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadStats = async () => {
  try {
    const data = await adminApi.getDashboardData()
    if (data) {
      stats.totalCars = data.totalCars || 0
      stats.totalOrders = data.totalOrders || 0
      stats.totalUsers = data.totalUsers || 0
      stats.totalRevenue = data.totalRevenue || '0.00'
      
      carStats.available = data.availableCars || 0
      carStats.rented = data.rentedCars || 0
      carStats.repair = data.repairCars || 0
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecentOrders = async () => {
  ordersLoading.value = true
  try {
    const data = await adminApi.getOrders({ limit: 5, sort: 'createTime', order: 'desc' })
    recentOrders.value = data?.records || data || []
  } catch (error) {
    console.error('加载订单列表失败:', error)
  } finally {
    ordersLoading.value = false
  }
}

// 加载所有数据的方法
const loadAllData = () => {
  loadStats()
  loadRecentOrders()
}

onMounted(() => {
  loadAllData()
})

// 使用导航守卫 - 当路由更新但组件复用时触发
onBeforeRouteUpdate((to, from, next) => {
  // 当进入数据概览页面时刷新数据
  if (to.path === '/admin') {
    loadAllData()
  }
  next()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.dashboard-page {
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
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
    margin-bottom: 24px;
    
    @media (max-width: 1200px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
    
    .stat-card {
      background: $bg-primary;
      border-radius: 16px;
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 20px;
      box-shadow: $shadow-card;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: $shadow-card-hover;
      }
      
      .stat-icon {
        width: 64px;
        height: 64px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        
        &.cars {
          background: linear-gradient(135deg, #3b82f6, #60a5fa);
        }
        
        &.orders {
          background: linear-gradient(135deg, #8b5cf6, #a78bfa);
        }
        
        &.users {
          background: linear-gradient(135deg, #10b981, #34d399);
        }
        
        &.revenue {
          background: linear-gradient(135deg, #f59e0b, #fbbf24);
        }
      }
      
      .stat-info {
        display: flex;
        flex-direction: column;
        
        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: $text-primary;
          line-height: 1.2;
        }
        
        .stat-label {
          font-size: 14px;
          color: $text-tertiary;
          margin-top: 4px;
        }
      }
    }
  }
  
  .content-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 24px;
    margin-bottom: 24px;
    
    @media (max-width: 1024px) {
      grid-template-columns: 1fr;
    }
    
    .card {
      background: $bg-primary;
      border-radius: 16px;
      padding: 24px;
      box-shadow: $shadow-card;
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        
        h3 {
          font-size: 18px;
          font-weight: 600;
          color: $text-primary;
        }
      }
    }
    
    .recent-orders {
      .amount {
        font-weight: 600;
        color: $danger-color;
      }
      
      :deep(.el-table) {
        --el-table-border-color: #{$border-light};
        --el-table-header-bg-color: #{$bg-tertiary};
        
        th.el-table__cell {
          font-weight: 600;
          color: $text-secondary;
        }
      }
    }
    
    .quick-actions {
      .actions-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;
        
        .action-item {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 12px;
          padding: 24px 16px;
          background: $bg-tertiary;
          border-radius: 12px;
          cursor: pointer;
          transition: all 0.2s ease;
          
          &:hover {
            background: rgba($primary-color, 0.1);
            transform: translateY(-2px);
            
            .action-icon {
              background: $primary-color;
              color: #fff;
            }
          }
          
          .action-icon {
            width: 48px;
            height: 48px;
            background: rgba($primary-color, 0.1);
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: $primary-color;
            transition: all 0.2s ease;
          }
          
          span {
            font-size: 14px;
            font-weight: 500;
            color: $text-secondary;
          }
        }
      }
    }
  }
  
  .car-status {
    background: $bg-primary;
    border-radius: 16px;
    padding: 24px;
    box-shadow: $shadow-card;
    
    .card-header {
      margin-bottom: 20px;
      
      h3 {
        font-size: 18px;
        font-weight: 600;
        color: $text-primary;
      }
    }
    
    .status-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 24px;
      
      @media (max-width: 640px) {
        grid-template-columns: 1fr;
      }
      
      .status-item {
        text-align: center;
        padding: 24px;
        border-radius: 12px;
        
        &.available {
          background: rgba($success-color, 0.1);
          
          .status-count {
            color: $success-color;
          }
        }
        
        &.rented {
          background: rgba($primary-color, 0.1);
          
          .status-count {
            color: $primary-color;
          }
        }
        
        &.repair {
          background: rgba($warning-color, 0.1);
          
          .status-count {
            color: $warning-color;
          }
        }
        
        .status-count {
          font-size: 36px;
          font-weight: 700;
          line-height: 1;
          margin-bottom: 8px;
        }
        
        .status-label {
          font-size: 14px;
          color: $text-secondary;
        }
      }
    }
  }
}
</style>
