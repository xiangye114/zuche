<template>
  <div class="register-page">
    <div class="register-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>
    
    <div class="register-container">
      <div class="register-card">
        <div class="card-header">
          <div class="brand">
            <el-icon :size="36"><Car /></el-icon>
            <span>在线租车</span>
          </div>
          <h2>创建账号</h2>
          <p>注册即可开始您的租车之旅</p>
        </div>
        
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="register-form">
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="12">
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
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input 
                  v-model="form.confirmPassword" 
                  type="password" 
                  placeholder="请确认密码"
                  size="large"
                  show-password
                  :prefix-icon="Lock"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input 
                  v-model="form.phone" 
                  placeholder="请输入手机号"
                  size="large"
                  :prefix-icon="Phone"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input 
                  v-model="form.email" 
                  placeholder="请输入邮箱"
                  size="large"
                  :prefix-icon="Message"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button 
              type="primary" 
              :loading="loading" 
              @click="handleRegister" 
              class="register-btn"
              size="large"
            >
              {{ loading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="card-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await authApi.register({
      username: form.username,
      password: form.password,
      phone: form.phone,
      email: form.email
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 50%, #0891b2 100%);
  position: relative;
  overflow: hidden;
  padding: 40px 20px;
}

.register-bg {
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
    left: -100px;
    animation-delay: 0s;
  }
  
  .bg-shape-2 {
    width: 400px;
    height: 400px;
    background: #fff;
    bottom: -100px;
    right: -100px;
    animation-delay: -5s;
  }
  
  .bg-shape-3 {
    width: 300px;
    height: 300px;
    background: #fff;
    top: 40%;
    right: 20%;
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

.register-container {
  z-index: 1;
  width: 100%;
  max-width: 520px;
}

.register-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  
  @media (max-width: 480px) {
    padding: 32px 24px;
  }
  
  .card-header {
    text-align: center;
    margin-bottom: 32px;
    
    .brand {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 20px;
      color: $primary-color;
      
      span {
        font-size: 20px;
        font-weight: 700;
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
  
  .register-form {
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
    
    .register-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 12px;
      background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
      border: none;
      box-shadow: 0 4px 14px rgba($primary-color, 0.35);
      transition: all 0.3s ease;
      margin-top: 8px;
      
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
    
    .login-link {
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
</style>
