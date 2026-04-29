import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 1)
  
  const login = async (loginForm) => {
    const res = await authApi.login(loginForm)
    token.value = res.token
    user.value = res.user
    localStorage.setItem('token', res.token)
    localStorage.setItem('user', JSON.stringify(res.user))
    return res
  }
  
  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }
  
  const fetchUserInfo = async () => {
    if (!token.value) return
    try {
      const res = await authApi.getInfo()
      user.value = res
      localStorage.setItem('user', JSON.stringify(res))
    } catch (error) {
      logout()
    }
  }
  
  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    login,
    logout,
    fetchUserInfo
  }
})
