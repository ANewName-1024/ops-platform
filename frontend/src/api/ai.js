import request from '../utils/request'

// 通过 Nginx 代理到 Gateway，再路由到 chat-service

// 兼容旧名称
export const chatAI = sendMessage

/**
 * 获取 AI 对话响应
 */
export function sendMessage(content, sessionId) {
  return request({
    url: '/chat/send',
    method: 'post',
    data: { content, sessionId }
  })
}

/**
 * 获取会话列表
 */
export function getSessions() {
  return request({
    url: '/chat/sessions',
    method: 'get'
  })
}

/**
 * 创建新会话
 */
export function createSession(params) {
  return request({
    url: '/chat/sessions',
    method: 'post',
    data: params
  })
}

/**
 * 获取会话历史
 */
export function getHistory(sessionId) {
  return request({
    url: `/chat/history/${sessionId}`,
    method: 'get'
  })
}

/**
 * 删除会话
 */
export function deleteSession(sessionId) {
  return request({
    url: `/chat/sessions/${sessionId}`,
    method: 'delete'
  })
}

/**
 * 获取 AI 健康状态
 */
export function getAIHealth() {
  return request({
    url: '/chat/health',
    method: 'get'
  })
}
