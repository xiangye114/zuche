import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'https://disabled-diabetes-sudoku.ngrok-free.dev',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
    'Accept': 'application/json;charset=UTF-8'
  },
  responseType: 'json',
  responseEncoding: 'utf8'
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    console.log('[Request Interceptor] URL:', config.url, 'Token:', token ? '存在' : '不存在')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('[Request Interceptor] Error:', error)
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    console.log('[Response Interceptor] URL:', response.config.url, 'Code:', res.code)
    // 如果已经是数据直接返回（处理某些特殊情况）
    if (!res || typeof res.code === 'undefined') {
      return res
    }
    // 统一处理后端返回格式
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data  // 直接返回 data 部分
  },
  error => {
    console.error('[Response Interceptor] Error:', error)
    console.error('[Response Interceptor] Error Config:', error.config)
    console.error('[Response Interceptor] Error Response:', error.response)
    
    if (error.response) {
      const { status, data } = error.response
      console.error('[Response Interceptor] Status:', status, 'Data:', data)
      
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        ElMessage.error('登录已过期，请重新登录')
        window.location.href = '/login'
      } else if (status === 403) {
        ElMessage.error('无权限访问')
      } else if (status === 500) {
        ElMessage.error(data?.message || '服务器内部错误')
      } else {
        ElMessage.error(data?.message || error.message || '网络请求失败')
      }
    } else if (error.request) {
      console.error('[Response Interceptor] Request made but no response received')
      ElMessage.error('请求已发送，但未收到响应')
    } else {
      console.error('[Response Interceptor] Error setting up request:', error.message)
      ElMessage.error('请求设置错误: ' + error.message)
    }
    return Promise.reject(error)
  }
)

export default request
