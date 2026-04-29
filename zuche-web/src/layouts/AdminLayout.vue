<template>
  <div class="admin-layout">
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
        <div class="logo">
          <el-icon :size="28"><Van /></el-icon>
          <span v-show="!isCollapse" class="logo-text">租车管理系统</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-left">
            <el-button text @click="isCollapse = !isCollapse" class="collapse-btn">
              <el-icon :size="20">
                <Fold v-if="!isCollapse" />
                <Expand v-else />
              </el-icon>
            </el-button>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
                {{ item }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          
          <div class="header-right">
            <el-button text @click="goToFrontend" class="back-btn">
              <el-icon><HomeFilled /></el-icon>
              <span>返回前台</span>
            </el-button>
            
            <el-dropdown @command="handleCommand">
              <div class="user-info">
                <el-avatar :size="36" class="avatar">
                  {{ userStore.user?.username?.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ userStore.user?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人信息
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="main">
          <router-view :key="route.path" />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'
import { hasMenuPermission, hasStoreManagePermission, RoleNameMap } from '@/utils/permission'
import {
  Van, DataAnalysis, Document, User, OfficeBuilding,
  Medal, Grid, Fold, Expand, ArrowDown, SwitchButton, Collection, HomeFilled
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => {
  return route.path
})

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => item.meta.title)
})

// 菜单权限控制
const menuItems = computed(() => [
  { index: '/admin', icon: 'DataAnalysis', title: '数据概览', key: 'dashboard', show: hasMenuPermission('dashboard') },
  { index: '/admin/cars', icon: 'Van', title: '车辆管理', key: 'cars', show: hasMenuPermission('cars') },
  { index: '/admin/orders', icon: 'Document', title: '订单管理', key: 'orders', show: hasMenuPermission('orders') },
  { index: '/admin/users', icon: 'User', title: '用户管理', key: 'users', show: hasMenuPermission('users') },
  { index: '/admin/stores', icon: 'OfficeBuilding', title: '门店管理', key: 'stores', show: hasMenuPermission('stores') },
  { index: '/admin/brands', icon: 'Medal', title: '品牌管理', key: 'brands', show: hasMenuPermission('brands') },
  { index: '/admin/categories', icon: 'Grid', title: '分类管理', key: 'categories', show: hasMenuPermission('categories') },
  { index: '/admin/knowledge', icon: 'Collection', title: '知识库管理', key: 'knowledge', show: hasMenuPermission('knowledge') }
].filter(item => item.show))

const handleMenuSelect = (index) => {
  if (index && index !== route.path) {
    router.push(index)
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      router.push('/login')
    } catch {}
  } else if (command === 'profile') {
    router.push('/user-center')
  }
}

const goToFrontend = () => {
  router.push('/')
}

// 获取角色名称
const roleName = computed(() => {
  return RoleNameMap[userStore.user?.role] || '普通用户'
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.admin-layout {
  height: 100vh;
  
  .el-container {
    height: 100%;
  }
  
  .sidebar {
    background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
    transition: width 0.3s ease;
    overflow: hidden;
    
    .logo {
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      padding: 0 20px;
      color: #fff;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
      
      .el-icon {
        color: $primary-light;
      }
      
      .logo-text {
        font-size: 18px;
        font-weight: 700;
        white-space: nowrap;
        background: linear-gradient(135deg, #fff 0%, #94a3b8 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }
    }
    
    .sidebar-menu {
      border: none;
      background: transparent;
      
      :deep(.el-menu-item),
      :deep(.el-sub-menu__title) {
        color: #94a3b8;
        height: 50px;
        line-height: 50px;
        margin: 4px 12px;
        border-radius: 8px;
        
        &:hover {
          background: rgba(255, 255, 255, 0.05);
          color: #fff;
        }
        
        .el-icon {
          font-size: 18px;
        }
      }
      
      :deep(.el-menu-item.is-active) {
        background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
        color: #fff;
      }
      

    }
  }
  
  .header {
    background: $bg-primary;
    border-bottom: 1px solid $border-light;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .collapse-btn {
        padding: 8px;
        border-radius: 8px;
        
        &:hover {
          background: $bg-tertiary;
        }
      }
      
      :deep(.el-breadcrumb) {
        font-size: 14px;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .back-btn {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border-radius: 8px;
        color: $text-secondary;
        
        &:hover {
          background: $bg-tertiary;
          color: $primary-color;
        }
        
        .el-icon {
          font-size: 16px;
        }
        
        span {
          font-size: 14px;
          font-weight: 500;
        }
      }
      
      .user-info {
        display: flex;
        align-items: center;
        gap: 10px;
        cursor: pointer;
        padding: 6px 12px;
        border-radius: 8px;
        transition: background 0.2s ease;
        
        &:hover {
          background: $bg-tertiary;
        }
        
        .avatar {
          background: linear-gradient(135deg, $primary-color, $primary-light);
          color: #fff;
          font-weight: 600;
        }
        
        .username {
          font-weight: 500;
          color: $text-primary;
        }
      }
    }
  }
  
  .main {
    background: $bg-secondary;
    padding: 24px;
    overflow-y: auto;
  }
}
</style>
