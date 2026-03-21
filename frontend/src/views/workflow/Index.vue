<template>
  <div class="workflow-page">
    <div class="page-header">
      <h1>工作流</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>新建工作流
        </el-button>
      </div>
    </div>
    
    <!-- 状态筛选 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="执行中" name="running" />
      <el-tab-pane label="已完成" name="completed" />
      <el-tab-pane label="失败" name="failed" />
    </el-tabs>
    
    <!-- 工作流列表 -->
    <el-card>
      <el-table :data="workflows" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="工作流名称" min-width="150">
          <template #default="{ row }">
            <div class="workflow-name">
              <el-icon><Operation /></el-icon>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewWorkflow(row)">详情</el-button>
            <el-button size="small" type="primary" @click="executeWorkflow(row)" :loading="row.executing">
              执行
            </el-button>
            <el-button size="small" type="danger" @click="deleteWorkflow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 创建对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建工作流" width="600px">
      <el-form :model="workflowForm" label-width="100px">
        <el-form-item label="工作流名称">
          <el-input v-model="workflowForm.name" placeholder="请输入工作流名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="workflowForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="流程配置">
          <div class="workflow-editor">
            <div class="step-list">
              <div v-for="(step, index) in workflowForm.steps" :key="index" class="step-item">
                <el-tag>{{ step.name }}</el-tag>
                <el-icon v-if="index < workflowForm.steps.length - 1"><ArrowRight /></el-icon>
              </div>
              <el-button size="small" text @click="addStep">
                <el-icon><Plus /></el-icon>添加步骤
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowRight, Operation } from '@element-plus/icons-vue'
import { getWorkflows, createWorkflow, executeWorkflow as executeApi, deleteWorkflow as deleteApi } from '../../api/workflow'

const loading = ref(false)
const creating = ref(false)
const activeTab = ref('all')
const showCreateDialog = ref(false)

const workflows = ref([])

const workflowForm = reactive({
  name: '',
  description: '',
  steps: [
    { name: '开始', type: 'start' },
    { name: '审批', type: 'approval' },
    { name: '执行', type: 'execute' },
    { name: '结束', type: 'end' }
  ]
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const getStatusType = (status) => {
  const map = { running: 'primary', completed: 'success', failed: 'danger', pending: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { running: '执行中', completed: '已完成', failed: '失败', pending: '待执行' }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getWorkflows()
    workflows.value = res.workflows || []
    pagination.total = workflows.value.length
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  loadData()
}

const handleSizeChange = () => {
  loadData()
}

const handleCurrentChange = () => {
  loadData()
}

const viewWorkflow = (row) => {
  ElMessage.info(`查看工作流: ${row.name}`)
}

const executeWorkflow = async (row) => {
  row.executing = true
  try {
    await executeApi(row.id)
    ElMessage.success('执行成功')
    loadData()
  } catch (e) {
    ElMessage.error('执行失败')
  } finally {
    row.executing = false
  }
}

const deleteWorkflow = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除工作流 "${row.name}" 吗？`, '提示', { type: 'warning' })
    await deleteApi(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleCreate = async () => {
  if (!workflowForm.name) {
    ElMessage.warning('请输入工作流名称')
    return
  }
  creating.value = true
  try {
    await createWorkflow(workflowForm)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    loadData()
  } catch (e) {
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

const addStep = () => {
  ElMessage.info('添加步骤功能开发中')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.workflow-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }

.workflow-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.workflow-editor {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  min-height: 100px;
}

.step-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
