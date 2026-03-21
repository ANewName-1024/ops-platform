import request from '../utils/request'

const API_BASE = '/ops'

// ==================== 运维 ====================
export function getTasks() {
  return request({
    url: `${API_BASE}/tasks`,
    method: 'get'
  })
}

export function createTask(data) {
  return request({
    url: `${API_BASE}/tasks`,
    method: 'post',
    data
  })
}

export function getTask(taskId) {
  return request({
    url: `${API_BASE}/tasks/${taskId}`,
    method: 'get'
  })
}

export function executeScript(data) {
  return request({
    url: `${API_BASE}/scripts/execute`,
    method: 'post',
    data
  })
}

// ==================== 证书 ====================
export function getCertificateInfo() {
  return request({
    url: `${API_BASE}/cert/info`,
    method: 'get'
  })
}

export function rotateCertificate() {
  return request({
    url: `${API_BASE}/cert/rotate`,
    method: 'post'
  })
}

export function checkCertificate() {
  return request({
    url: `${API_BASE}/cert/check`,
    method: 'get'
  })
}
