import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '../api/auth'

const TOKEN_KEY = import.meta.env.VITE_TOKEN_KEY || 'ops_token'
const REFRESH_TOKEN_KEY = import.meta.env.VITE_REFRESH_TOKEN_KEY || 'ops_refresh_token'
const USER_INFO_KEY = import.meta.env.VITE_USER_INFO_KEY || 'ops_user_info'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    refreshToken: localStorage.getItem(REFRESH_TOKEN_KEY) || '',
    userInfo: JSON.parse(localStorage.getItem(USER_INFO_KEY) || 'null'),
    permissions: []
  }),

  getters: {
    isLoggedIn: state => !!state.token,
    username: state => state.userInfo?.username || '',
    roles: state => state.userInfo?.roles || []
  },

  actions: {
    /**
     * 用户登录
     */
    async login(username, password) {
      const res = await loginApi(username, password)
      
      if (res.token) {
        this.token = res.token
        this.refreshToken = res.refreshToken || ''
        
        // 存储 token
        localStorage.setItem(TOKEN_KEY, res.token)
        if (res.refreshToken) {
          localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken)
        }
        
        // 从登录响应中获取用户信息
        this.userInfo = {
          username: res.username,
          roles: res.roles || [],
          permissions: res.permissions || []
        }
        this.permissions = res.permissions || []
        
        // 缓存用户信息
        localStorage.setItem(USER_INFO_KEY, JSON.stringify(this.userInfo))
        
        return res
      }
      
      throw new Error('登录失败')
    },

    /**
     * 获取用户信息
     */
    async fetchUserInfo() {
      // 用户信息已包含在登录响应中
      return this.userInfo
    },

    /**
     * 退出登录
     */
    async logout() {
      try {
        await logoutApi()
      } catch (error) {
        console.error('登出请求失败:', error)
      }
      
      // 清除状态
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
      this.permissions = []
      
      // 清除本地存储
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(REFRESH_TOKEN_KEY)
      localStorage.removeItem(USER_INFO_KEY)
    },

    /**
     * 检查是否有指定权限
     */
    hasPermission(permission) {
      return this.permissions.includes(permission)
    },

    /**
     * 检查是否有指定角色
     */
    hasRole(role) {
      return this.roles.includes(role)
    }
  }
})
