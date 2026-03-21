import request from '../utils/request'

const API_BASE = '/ops'

// ==================== 仪表盘 ====================
export function getDashboardOverview() {
  return request({
    url: `${API_BASE}/dashboard/overview`,
    method: 'get'
  })
}

export function getDashboardActions() {
  return request({
    url: `${API_BASE}/dashboard/actions`,
    method: 'get'
  })
}

export function getDashboardActivity() {
  return request({
    url: `${API_BASE}/dashboard/activity`,
    method: 'get'
  })
}

// ==================== 监控 ====================
export function getMetrics() {
  return request({
    url: `${API_BASE}/metrics`,
    method: 'get'
  })
}

export function getHealth() {
  return request({
    url: `${API_BASE}/health`,
    method: 'get'
  })
}

export function getLoggers() {
  return request({
    url: `${API_BASE}/loggers`,
    method: 'get'
  })
}

export function setLoggerLevel(name, level) {
  return request({
    url: `${API_BASE}/loggers/${name}/${level}`,
    method: 'post'
  })
}

export function getEnv() {
  return request({
    url: `${API_BASE}/env`,
    method: 'get'
  })
}

export function getLogs(params) {
  return request({
    url: `${API_BASE}/logs`,
    method: 'get',
    params
  })
}
