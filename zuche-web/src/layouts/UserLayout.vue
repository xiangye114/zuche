<template>
  <div class="user-layout">
    <header class="header">
      <div class="container header-content">
        <div class="logo" @click="$router.push('/')">
          <el-icon :size="32"><Van /></el-icon>
          <span class="logo-text">在线租车</span>
        </div>
        
        <nav class="nav">
          <router-link to="/" class="nav-item">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link to="/cars" class="nav-item">
            <el-icon><Van /></el-icon>
            <span>车辆列表</span>
          </router-link>
          <router-link to="/chat" class="nav-item" v-if="userStore.isLoggedIn">
            <el-icon><Message /></el-icon>
            <span>在线客服</span>
          </router-link>
        </nav>
        
        <div class="user-area">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown @command="handleCommand" trigger="click">
              <div class="user-info">
                <el-avatar :size="36" :src="userStore.user?.avatar" class="user-avatar">
                  {{ userStore.user?.username?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <span class="username">{{ userStore.user?.username }}</span>
                <el-icon class="arrow-icon"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <el-dropdown-item command="user">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <el-icon><Document /></el-icon>
                    我的订单
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>
                    <el-icon><Setting /></el-icon>
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" class="login-btn" @click="$router.push('/login')">
              登录
            </el-button>
            <el-button class="register-btn" @click="$router.push('/register')">
              注册
            </el-button>
          </template>
        </div>
      </div>
    </header>
    
    <main class="main">
      <router-view />
    </main>
    
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="logo">
              <el-icon :size="28"><Car /></el-icon>
              <span>在线租车</span>
            </div>
            <p class="slogan">轻松出行，从租车开始</p>
          </div>
          <div class="footer-links">
            <div class="link-group">
              <h4>快速链接</h4>
              <router-link to="/">首页</router-link>
              <router-link to="/cars">车辆列表</router-link>
            </div>
            <div class="link-group">
              <h4>用户服务</h4>
              <router-link to="/user">个人中心</router-link>
              <router-link to="/orders">我的订单</router-link>
            </div>
            <div class="link-group">
              <h4>联系我们</h4>
              <span>客服热线: 400-888-8888</span>
              <span>服务时间: 24小时</span>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2024 在线租车网站 - 毕业设计作品</p>
        </div>
      </div>
    </footer>
    
    <ChatWidget v-if="userStore.isLoggedIn" />
  </div>
</template>

<script setup>
import { useUserStore } from '@/store/user'
import { useRouter } from 'vue-router'
import ChatWidget from '@/components/ChatWidget.vue'
import { Van, HomeFilled, User, Document, Setting, SwitchButton, ArrowDown, Message } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const handleCommand = (command) => {
  switch (command) {
    case 'user':
      router.push('/user')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      userStore.logout()
      break
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: $bg-primary;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 1px 2px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: $z-sticky;
  
  .header-content {
    height: $header-height;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  
  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    transition: transform 0.2s ease;
    
    &:hover {
      transform: scale(1.02);
    }
    
    .el-icon {
      color: $primary-color;
    }
    
    .logo-text {
      font-size: 22px;
      font-weight: 700;
      background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
  
  .nav {
    display: flex;
    gap: 8px;
    
    .nav-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 10px 20px;
      border-radius: $radius-lg;
      color: $text-secondary;
      font-size: 15px;
      font-weight: 500;
      transition: all 0.2s ease;
      
      .el-icon {
        font-size: 18px;
      }
      
      &:hover {
        color: $primary-color;
        background: rgba($primary-color, 0.08);
      }
      
      &.router-link-active {
        color: $primary-color;
        background: rgba($primary-color, 0.1);
      }
    }
    
    @media (max-width: 768px) {
      display: none;
    }
  }
  
  .user-area {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 6px 12px 6px 6px;
      border-radius: $radius-full;
      cursor: pointer;
      transition: all 0.2s ease;
      
      &:hover {
        background: $bg-tertiary;
      }
      
      .user-avatar {
        background: linear-gradient(135deg, $primary-color, $primary-light);
        color: #fff;
        font-weight: 600;
      }
      
      .username {
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-weight: 500;
        color: $text-primary;
      }
      
      .arrow-icon {
        color: $text-tertiary;
        font-size: 12px;
        transition: transform 0.2s ease;
      }
    }
    
    .login-btn {
      border-radius: $radius-lg;
      font-weight: 500;
      padding: 10px 24px;
    }
    
    .register-btn {
      border-radius: $radius-lg;
      font-weight: 500;
      padding: 10px 24px;
      border-color: $border-color;
      
      &:hover {
        border-color: $primary-color;
        color: $primary-color;
      }
    }
  }
}

.main {
  flex: 1;
  background: $bg-secondary;
}

.footer {
  background: $bg-dark;
  color: rgba(255, 255, 255, 0.85);
  padding: 48px 0 24px;
  
  .footer-content {
    display: flex;
    justify-content: space-between;
    gap: 60px;
    padding-bottom: 32px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    
    @media (max-width: 768px) {
      flex-direction: column;
      gap: 32px;
    }
  }
  
  .footer-brand {
    .logo {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 12px;
      
      .el-icon {
        color: $primary-light;
      }
      
      span {
        font-size: 20px;
        font-weight: 700;
        color: #fff;
      }
    }
    
    .slogan {
      color: rgba(255, 255, 255, 0.6);
      font-size: 14px;
    }
  }
  
  .footer-links {
    display: flex;
    gap: 60px;
    
    @media (max-width: 768px) {
      flex-wrap: wrap;
      gap: 32px;
    }
    
    .link-group {
      display: flex;
      flex-direction: column;
      gap: 12px;
      
      h4 {
        font-size: 15px;
        font-weight: 600;
        color: #fff;
        margin-bottom: 4px;
      }
      
      a, span {
        color: rgba(255, 255, 255, 0.65);
        font-size: 14px;
        transition: color 0.2s ease;
      }
      
      a:hover {
        color: $primary-light;
      }
    }
  }
  
  .footer-bottom {
    padding-top: 24px;
    text-align: center;
    
    p {
      color: rgba(255, 255, 255, 0.5);
      font-size: 13px;
    }
  }
}
</style>

<style lang="scss">
@import '@/assets/styles/variables.scss';

.user-dropdown {
  margin-top: 8px;
  border-radius: $radius-lg;
  padding: 8px;
  box-shadow: $shadow-xl;
  
  .el-dropdown-menu__item {
    border-radius: $radius-md;
    padding: 10px 16px;
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    
    .el-icon {
      font-size: 16px;
      color: $text-tertiary;
    }
    
    &:hover {
      background: $bg-tertiary;
      color: $primary-color;
      
      .el-icon {
        color: $primary-color;
      }
    }
  }
}
</style>
