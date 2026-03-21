import request from '../utils/request'

// 通过 Nginx 代理到 Gateway，再路由到对应服务
// Gateway 路由: /ops/** -> ops-service, /chat/** -> chat-service 等

/**
 * 用户登录
 */
export function login(username, password) {
  return request({
    url: '/ops/auth/login',
    method: 'post',
    data: { username, password }
  })
}

/**
 * 用户注册
 */
export function register(username, password, email) {
  return request({
    url: '/ops/auth/register',
    method: 'post',
    data: { username, password, email }
  })
}

/**
 * 刷新 Token
 */
export function refreshToken(refreshToken) {
  return request({
    url: '/ops/auth/refresh',
    method: 'post',
    data: { refreshToken }
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/ops/auth/logout',
    method: 'post'
  })
}

/**
 * 获取当前用户信息
 */
export function getCurrentUser() {
  const userInfo = localStorage.getItem('ops_user_info')
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 检查权限
 */
export function checkPermission(permission) {
  const userInfo = getCurrentUser()
  return userInfo?.permissions?.includes(permission) || false
}
