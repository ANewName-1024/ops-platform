import request from '../utils/request'

const API_BASE = '/ops'

// ==================== 自愈 ====================
export function getHealStrategies() {
  return request({
    url: `${API_BASE}/heal/strategies`,
    method: 'get'
  })
}

export function createHealStrategy(data) {
  return request({
    url: `${API_BASE}/heal/strategies`,
    method: 'post',
    data
  })
}

export function deleteHealStrategy(alertType) {
  return request({
    url: `${API_BASE}/heal/strategies/${alertType}`,
    method: 'delete'
  })
}

export function executeHeal(alertType) {
  return request({
    url: `${API_BASE}/heal/execute/${alertType}`,
    method: 'post'
  })
}

// ==================== 灰度发布 ====================
export function getReleases() {
  return request({
    url: `${API_BASE}/releases`,
    method: 'get'
  })
}

export function createRelease(data) {
  return request({
    url: `${API_BASE}/releases`,
    method: 'post',
    data
  })
}

export function getRelease(releaseId) {
  return request({
    url: `${API_BASE}/releases/${releaseId}`,
    method: 'get'
  })
}

export function updateTraffic(releaseId, traffic) {
  return request({
    url: `${API_BASE}/releases/${releaseId}/traffic`,
    method: 'put',
    data: { traffic }
  })
}

export function completeRelease(releaseId) {
  return request({
    url: `${API_BASE}/releases/${releaseId}/complete`,
    method: 'post'
  })
}

export function rollbackRelease(releaseId) {
  return request({
    url: `${API_BASE}/releases/${releaseId}/rollback`,
    method: 'post'
  })
}
