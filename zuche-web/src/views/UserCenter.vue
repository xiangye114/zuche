<template>
  <div class="user-center-page">
    <div class="container">
      <div class="page-header">
        <h1>个人中心</h1>
        <p>管理您的账户信息</p>
      </div>

      <div class="user-content">
        <div class="sidebar">
          <div class="user-card">
            <div class="user-avatar">
              <el-avatar :size="80" :src="userStore.user?.avatar">
                {{ userStore.user?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
            </div>
            <div class="user-info">
              <h3>{{ userStore.user?.username }}</h3>
              <p>{{ userStore.user?.phone || '未绑定手机' }}</p>
            </div>
          </div>

          <el-menu :default-active="activeMenu" class="sidebar-menu" @select="handleMenuSelect">
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>账户安全</span>
            </el-menu-item>
            <el-menu-item index="favorites">
              <el-icon><Star /></el-icon>
              <span>我的收藏</span>
            </el-menu-item>
          </el-menu>
        </div>

        <div class="main-content">
          <div class="content-card" v-show="activeMenu === 'profile'">
            <div class="card-header">
              <h2>个人信息</h2>
            </div>
            <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px" class="profile-form">
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" disabled />
              </el-form-item>
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="手机号码" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号码" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="身份证号" prop="idCard">
                <el-input v-model="profileForm.idCard" placeholder="请输入身份证号" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdateProfile" :loading="updateLoading">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="content-card" v-show="activeMenu === 'security'">
            <div class="card-header">
              <h2>修改密码</h2>
            </div>
            <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="password-form">
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">修改密码</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="content-card" v-show="activeMenu === 'favorites'">
            <div class="card-header">
              <h2>我的收藏</h2>
            </div>
            <div class="favorites-list" v-loading="favoritesLoading">
              <div class="favorite-item" v-for="car in favorites" :key="car.id" @click="goToCarDetail(car.id)">
                <div class="car-image">
                  <el-image :src="car.image || defaultImage" fit="cover" />
                </div>
                <div class="car-info">
                  <h4>{{ car.name }}</h4>
                  <div class="car-tags">
                    <el-tag size="small" v-if="car.brandName" effect="plain">{{ car.brandName }}</el-tag>
                    <el-tag size="small" type="info" v-if="car.categoryName" effect="plain">{{ car.categoryName }}</el-tag>
                  </div>
                  <div class="car-price">
                    <span class="price">¥{{ car.pricePerDay }}</span>
                    <span class="unit">/天</span>
                  </div>
                </div>
                <div class="car-actions">
                  <el-button type="danger" text @click.stop="handleRemoveFavorite(car.id)">
                    <el-icon><Delete /></el-icon>
                    取消收藏
                  </el-button>
                </div>
              </div>
              <el-empty v-if="!favoritesLoading && favorites.length === 0" description="暂无收藏的车辆" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { userApi } from '@/api/user'
import { carApi } from '@/api/car'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('profile')
const updateLoading = ref(false)
const passwordLoading = ref(false)
const favoritesLoading = ref(false)
const favorites = ref([])
const defaultImage = 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=400&h=300&fit=crop'

const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const profileForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  idCard: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

const validateIdCard = (rule, value, callback) => {
  if (value && !/^\d{17}[\dXx]$/.test(value)) {
    callback(new Error('请输入正确的身份证号'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const profileRules = {
  realName: [{ max: 20, message: '姓名不能超过20个字符', trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  idCard: [{ validator: validateIdCard, trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === 'favorites') {
    loadFavorites()
  }
}

const loadUserProfile = async () => {
  try {
    const res = await userApi.getProfile()
    profileForm.username = res.username
    profileForm.realName = res.realName || ''
    profileForm.phone = res.phone || ''
    profileForm.email = res.email || ''
    profileForm.idCard = res.idCard || ''
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const loadFavorites = async () => {
  favoritesLoading.value = true
  try {
    const res = await carApi.getFavorites()
    favorites.value = res || []
  } finally {
    favoritesLoading.value = false
  }
}

const handleUpdateProfile = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  updateLoading.value = true
  try {
    await userApi.updateProfile({
      realName: profileForm.realName,
      phone: profileForm.phone,
      email: profileForm.email,
      idCard: profileForm.idCard
    })
    ElMessage.success('保存成功')
    userStore.fetchUserInfo()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    updateLoading.value = false
  }
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordLoading.value = true
  try {
    await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordFormRef.value.resetFields()
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    passwordLoading.value = false
  }
}

const handleRemoveFavorite = async (carId) => {
  try {
    await carApi.removeFavorite(carId)
    favorites.value = favorites.value.filter(car => car.id !== carId)
    ElMessage.success('已取消收藏')
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('操作失败')
  }
}

const goToCarDetail = (carId) => {
  router.push(`/cars/${carId}`)
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.user-center-page {
  padding: 32px 0;
  min-height: calc(100vh - 64px);
}

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

.user-content {
  display: flex;
  gap: 24px;
  
  @media (max-width: 768px) {
    flex-direction: column;
  }
  
  .sidebar {
    width: 280px;
    flex-shrink: 0;
    
    @media (max-width: 768px) {
      width: 100%;
    }
    
    .user-card {
      background: $bg-primary;
      border-radius: 16px;
      padding: 24px;
      text-align: center;
      margin-bottom: 16px;
      box-shadow: $shadow-card;
      
      .user-avatar {
        margin-bottom: 16px;
        
        :deep(.el-avatar) {
          background: linear-gradient(135deg, $primary-color, $primary-light);
          color: #fff;
          font-size: 28px;
          font-weight: 600;
        }
      }
      
      .user-info {
        h3 {
          font-size: 18px;
          font-weight: 600;
          color: $text-primary;
          margin-bottom: 4px;
        }
        
        p {
          font-size: 14px;
          color: $text-tertiary;
        }
      }
    }
    
    .sidebar-menu {
      background: $bg-primary;
      border-radius: 16px;
      border: none;
      padding: 8px;
      box-shadow: $shadow-card;
      
      :deep(.el-menu-item) {
        border-radius: 10px;
        margin: 4px 0;
        height: 48px;
        
        &.is-active {
          background: rgba($primary-color, 0.1);
          color: $primary-color;
        }
        
        &:hover:not(.is-active) {
          background: $bg-tertiary;
        }
      }
    }
  }
  
  .main-content {
    flex: 1;
    min-width: 0;
    
    .content-card {
      background: $bg-primary;
      border-radius: 16px;
      padding: 32px;
      box-shadow: $shadow-card;
      
      .card-header {
        margin-bottom: 24px;
        padding-bottom: 16px;
        border-bottom: 1px solid $border-light;
        
        h2 {
          font-size: 20px;
          font-weight: 600;
          color: $text-primary;
        }
      }
      
      .profile-form,
      .password-form {
        max-width: 480px;
        
        :deep(.el-input__wrapper) {
          border-radius: 10px;
        }
        
        :deep(.el-button) {
          border-radius: 10px;
          font-weight: 500;
        }
      }
      
      .favorites-list {
        .favorite-item {
          display: flex;
          align-items: center;
          gap: 20px;
          padding: 16px;
          background: $bg-tertiary;
          border-radius: 12px;
          margin-bottom: 12px;
          cursor: pointer;
          transition: all 0.2s ease;
          
          &:hover {
            background: darken($bg-tertiary, 2%);
            transform: translateX(4px);
          }
          
          .car-image {
            width: 120px;
            height: 80px;
            border-radius: 8px;
            overflow: hidden;
            flex-shrink: 0;
            
            .el-image {
              width: 100%;
              height: 100%;
            }
          }
          
          .car-info {
            flex: 1;
            min-width: 0;
            
            h4 {
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
              margin-bottom: 8px;
            }
            
            .car-price {
              .price {
                font-size: 18px;
                font-weight: 700;
                color: $danger-color;
              }
              
              .unit {
                font-size: 13px;
                color: $text-tertiary;
              }
            }
          }
          
          .car-actions {
            flex-shrink: 0;
          }
        }
      }
    }
  }
}
</style>
