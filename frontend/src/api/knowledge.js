import request from '../utils/request'

const API_BASE = '/knowledge'

/**
 * 获取知识库列表
 */
export function getKnowledgeList(params) {
  return request({
    url: `${API_BASE}/bases`,
    method: 'get',
    params
  })
}

// 兼容旧名称
export const getKnowledgeBases = getKnowledgeList

/**
 * 获取知识库详情
 */
export function getKnowledgeDetail(id) {
  return request({
    url: `${API_BASE}/bases/${id}`,
    method: 'get'
  })
}

/**
 * 创建知识库
 */
export function createKnowledge(data) {
  return request({
    url: `${API_BASE}/bases`,
    method: 'post',
    data
  })
}

// 兼容旧名称
export const createKnowledgeBase = createKnowledge

/**
 * 更新知识库
 */
export function updateKnowledge(id, data) {
  return request({
    url: `${API_BASE}/bases/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除知识库
 */
export function deleteKnowledge(id) {
  return request({
    url: `${API_BASE}/bases/${id}`,
    method: 'delete'
  })
}

// 兼容旧名称
export const deleteKnowledgeBase = deleteKnowledge

/**
 * 获取文档列表
 */
export function getDocumentList(kbId, params) {
  return request({
    url: `${API_BASE}/bases/${kbId}/documents`,
    method: 'get',
    params
  })
}

// 兼容旧名称
export const getDocuments = getDocumentList

/**
 * 添加文档
 */
export function addDocument(kbId, data) {
  return request({
    url: `${API_BASE}/bases/${kbId}/documents`,
    method: 'post',
    data
  })
}

/**
 * 删除文档
 */
export function deleteDocument(id) {
  return request({
    url: `${API_BASE}/documents/${id}`,
    method: 'delete'
  })
}

/**
 * 搜索知识库
 */
export function searchKnowledge(query) {
  return request({
    url: `${API_BASE}/search`,
    method: 'get',
    params: { q: query }
  })
}
