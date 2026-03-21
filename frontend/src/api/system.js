import request from '../utils/request'

const API_BASE = '/ops'

// ==================== 系统配置 ====================
export function getSystemConfig() {
  return request({
    url: `${API_BASE}/system/config`,
    method: 'get'
  })
}

export function updateSystemConfig(config) {
  return request({
    url: `${API_BASE}/system/config`,
    method: 'put',
    data: config
  })
}

export function getConfigByKey(key) {
  return request({
    url: `${API_BASE}/system/config/${key}`,
    method: 'get'
  })
}

export function setConfigByKey(key, value) {
  return request({
    url: `${API_BASE}/system/config/${key}`,
    method: 'post',
    data: { value }
  })
}

export function getSystemInfo() {
  return request({
    url: `${API_BASE}/system/info`,
    method: 'get'
  })
}
