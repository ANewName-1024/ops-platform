import request from '../utils/request'

const API_BASE = '/user'

/**
 * 获取角色列表
 */
export function getRoles() {
  return request({
    url: '/user/roles',
    method: 'get'
  })
}

/**
 * 获取用户列表
 */
export function getUsers(params) {
  return request({
    url: API_BASE,
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 */
export function getUserDetail(id) {
  return request({
    url: `${API_BASE}/${id}`,
    method: 'get'
  })
}

/**
 * 创建用户
 */
export function createUser(data) {
  return request({
    url: API_BASE,
    method: 'post',
    data
  })
}

/**
 * 更新用户
 */
export function updateUser(id, data) {
  return request({
    url: `${API_BASE}/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 */
export function deleteUser(id) {
  return request({
    url: `${API_BASE}/${id}`,
    method: 'delete'
  })
}

/**
 * 更新用户状态
 */
export function updateUserStatus(id, enabled) {
  return request({
    url: `${API_BASE}/${id}/status`,
    method: 'put',
    data: { enabled }
  })
}

/**
 * 重置密码
 */
export function resetPassword(id) {
  return request({
    url: `${API_BASE}/${id}/reset-password`,
    method: 'post'
  })
}
