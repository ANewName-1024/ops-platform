import request from '../utils/request'

const API_BASE = '/notification'

/**
 * 获取通知列表
 */
export function getNotificationList(params) {
  return request({
    url: API_BASE,
    method: 'get',
    params
  })
}

/**
 * 获取通知详情
 */
export function getNotificationDetail(id) {
  return request({
    url: `${API_BASE}/${id}`,
    method: 'get'
  })
}

/**
 * 标记通知为已读
 */
export function markAsRead(id) {
  return request({
    url: `${API_BASE}/${id}/read`,
    method: 'put'
  })
}

/**
 * 标记所有通知为已读
 */
export function markAllAsRead() {
  return request({
    url: `${API_BASE}/read-all`,
    method: 'put'
  })
}

/**
 * 删除通知
 */
export function deleteNotification(id) {
  return request({
    url: `${API_BASE}/${id}`,
    method: 'delete'
  })
}

/**
 * 获取未读数量
 */
export function getUnreadCount() {
  return request({
    url: `${API_BASE}/unread-count`,
    method: 'get'
  })
}

/**
 * 发送通知
 */
export function sendNotification(data) {
  return request({
    url: API_BASE,
    method: 'post',
    data
  })
}
