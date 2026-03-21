import request from '../utils/request'

const API_BASE = '/workflow'

/**
 * 获取工作流列表
 */
export function getWorkflowList(params) {
  return request({
    url: API_BASE,
    method: 'get',
    params
  })
}

// 兼容旧名称
export const getWorkflows = getWorkflowList

/**
 * 获取工作流详情
 */
export function getWorkflowDetail(id) {
  return request({
    url: `${API_BASE}/${id}`,
    method: 'get'
  })
}

/**
 * 创建工作流
 */
export function createWorkflow(data) {
  return request({
    url: API_BASE,
    method: 'post',
    data
  })
}

// 兼容旧名称
export const createWorkflowInstance = createWorkflow

/**
 * 更新工作流
 */
export function updateWorkflow(id, data) {
  return request({
    url: `${API_BASE}/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除工作流
 */
export function deleteWorkflow(id) {
  return request({
    url: `${API_BASE}/${id}`,
    method: 'delete'
  })
}

/**
 * 执行工作流
 */
export function executeWorkflow(id, params) {
  return request({
    url: `${API_BASE}/${id}/execute`,
    method: 'post',
    data: params
  })
}

/**
 * 获取工作流执行历史
 */
export function getExecutionHistory(workflowId, params) {
  return request({
    url: `${API_BASE}/${workflowId}/executions`,
    method: 'get',
    params
  })
}
