import axios from 'axios'
import { useUserStore } from '../stores/user'
import router from '../router'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083'

// 创建 axios 实例
const request = axios.create({
  baseURL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem(import.meta.env.VITE_TOKEN_KEY || 'ops_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data
  },
  async error => {
    const originalRequest = error.config
    
    // 如果是 401 且没有重试过
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      
      // 刷新 token 失败，清除 token 并跳转到登录页
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      return Promise.reject(error)
    }
    
    // 其他错误
    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export default request
