<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>
    
    <div class="login-container">
      <div class="login-left">
        <div class="brand">
          <el-icon :size="48"><Van /></el-icon>
          <h1>在线租车</h1>
        </div>
        <p class="slogan">轻松出行，从租车开始</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><CircleCheck /></el-icon>
            <span>海量车型，任您选择</span>
          </div>
          <div class="feature-item">
            <el-icon><CircleCheck /></el-icon>
            <span>价格透明，无隐藏费用</span>
          </div>
          <div class="feature-item">
            <el-icon><CircleCheck /></el-icon>
            <span>全程保障，安心出行</span>
          </div>
        </div>
      </div>
      
      <div class="login-right">
        <div class="login-card">
          <div class="card-header">
            <h2>欢迎回来</h2>
            <p>请登录您的账号</p>
          </div>
          
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form">
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="请输入用户名" 
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="请输入密码" 
                size="large"
                show-password
                :prefix-icon="Lock"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                :loading="loading" 
                @click="handleLogin" 
                class="login-btn"
                size="large"
              >
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="card-footer">
            <span>还没有账号？</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Van, CircleCheck } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    setTimeout(() => {
      const redirect = route.query.redirect || '/'
      window.location.href = redirect
    }, 300)
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 50%, #0891b2 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  
  .bg-shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    animation: float 20s ease-in-out infinite;
  }
  
  .bg-shape-1 {
    width: 600px;
    height: 600px;
    background: #fff;
    top: -200px;
    right: -100px;
    animation-delay: 0s;
  }
  
  .bg-shape-2 {
    width: 400px;
    height: 400px;
    background: #fff;
    bottom: -100px;
    left: -100px;
    animation-delay: -5s;
  }
  
  .bg-shape-3 {
    width: 300px;
    height: 300px;
    background: #fff;
    top: 50%;
    left: 30%;
    animation-delay: -10s;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-30px) rotate(10deg);
  }
}

.login-container {
  display: flex;
  align-items: center;
  gap: 80px;
  z-index: 1;
  padding: 40px;
  
  @media (max-width: 1024px) {
    flex-direction: column;
    gap: 40px;
  }
}

.login-left {
  color: #fff;
  text-align: center;
  
  @media (max-width: 1024px) {
    text-align: center;
  }
  
  .brand {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    margin-bottom: 16px;
    
    h1 {
      font-size: 36px;
      font-weight: 700;
      letter-spacing: -0.02em;
    }
  }
  
  .slogan {
    font-size: 18px;
    opacity: 0.9;
    margin-bottom: 48px;
  }
  
  .features {
    display: flex;
    flex-direction: column;
    gap: 16px;
    
    .feature-item {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      font-size: 15px;
      opacity: 0.85;
      
      .el-icon {
        font-size: 20px;
        color: #34d399;
      }
    }
  }
}

.login-right {
  .login-card {
    width: 420px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    padding: 48px 40px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
    
    @media (max-width: 480px) {
      width: 100%;
      padding: 32px 24px;
    }
    
    .card-header {
      text-align: center;
      margin-bottom: 32px;
      
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
    
    .login-form {
      :deep(.el-form-item__label) {
        font-weight: 500;
        color: $text-secondary;
        padding-bottom: 8px;
      }
      
      :deep(.el-input__wrapper) {
        border-radius: 12px;
        padding: 4px 16px;
        box-shadow: 0 0 0 1px $border-color inset;
        transition: all 0.2s ease;
        
        &:hover {
          box-shadow: 0 0 0 1px $primary-light inset;
        }
        
        &.is-focus {
          box-shadow: 0 0 0 2px rgba($primary-color, 0.15), 0 0 0 1px $primary-color inset;
        }
      }
      
      :deep(.el-input__inner) {
        height: 44px;
        font-size: 15px;
      }
      
      .login-btn {
        width: 100%;
        height: 48px;
        font-size: 16px;
        font-weight: 600;
        border-radius: 12px;
        background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
        border: none;
        box-shadow: 0 4px 14px rgba($primary-color, 0.35);
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 24px rgba($primary-color, 0.45);
        }
        
        &:active {
          transform: translateY(0);
        }
      }
    }
    
    .card-footer {
      text-align: center;
      margin-top: 24px;
      padding-top: 24px;
      border-top: 1px solid $border-light;
      color: $text-tertiary;
      font-size: 14px;
      
      .register-link {
        color: $primary-color;
        font-weight: 500;
        margin-left: 4px;
        transition: color 0.2s ease;
        
        &:hover {
          color: $primary-dark;
        }
      }
    }
  }
}
</style>
