<template>
  <div class="page-container">
    <div class="page-header">
      <h1>灰度发布</h1>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>新建发布
      </el-button>
    </div>
    <el-card>
      <el-table :data="releases" style="width: 100%" v-loading="loading">
        <el-table-column prop="version" label="版本" />
        <el-table-column prop="service" label="服务" />
        <el-table-column prop="traffic" label="流量分配">
          <template #default="{ row }">
            <el-slider 
              v-model="row.traffic" 
              :min="0" 
              :max="100" 
              :disabled="row.status !== 'running'"
              @change="handleTrafficChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'success' : row.status === 'rollingback' ? 'warning' : 'primary'">
              {{ { completed: '已完成', running: '进行中', rollingback: '回滚中' }[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button 
              size="small" 
              type="success" 
              v-if="row.status === 'running'"
              @click="handleComplete(row)"
            >完成</el-button>
            <el-button 
              size="small" 
              type="warning" 
              v-if="row.status === 'running'"
              @click="handleRollback(row)"
            >回滚</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建发布对话框 -->
    <el-dialog v-model="createVisible" title="新建灰度发布" width="500px">
      <el-form :model="releaseForm" label-width="100px">
        <el-form-item label="版本">
          <el-input v-model="releaseForm.version" placeholder="如: v2.0.0" />
        </el-form-item>
        <el-form-item label="服务">
          <el-select v-model="releaseForm.service" placeholder="选择服务" style="width: 100%">
            <el-option label="ops-service" value="ops-service" />
            <el-option label="workflow-service" value="workflow-service" />
            <el-option label="knowledge-service" value="knowledge-service" />
            <el-option label="notification-service" value="notification-service" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始流量">
          <el-slider v-model="releaseForm.traffic" :min="0" :max="100" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="发布详情" width="500px">
      <el-descriptions :column="2" border v-if="currentRelease">
        <el-descriptions-item label="版本">{{ currentRelease.version }}</el-descriptions-item>
        <el-descriptions-item label="服务">{{ currentRelease.service }}</el-descriptions-item>
        <el-descriptions-item label="流量">{{ currentRelease.traffic }}%</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentRelease.status === 'completed' ? 'success' : 'primary'">
            {{ currentRelease.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRelease.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getReleases, 
  createRelease, 
  getRelease, 
  updateTraffic, 
  completeRelease, 
  rollbackRelease 
} from '../../api/autoheal'

const releases = ref([])
const loading = ref(false)
const createVisible = ref(false)
const detailVisible = ref(false)
const creating = ref(false)
const currentRelease = ref(null)

const releaseForm = ref({
  version: '',
  service: '',
  traffic: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReleases()
    releases.value = res.releases || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  releaseForm.value = { version: '', service: '', traffic: 0 }
  createVisible.value = true
}

const handleCreate = async () => {
  if (!releaseForm.value.version || !releaseForm.value.service) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  creating.value = true
  try {
    await createRelease(releaseForm.value)
    ElMessage.success('创建成功')
    createVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await getRelease(row.id)
    currentRelease.value = res
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const handleTrafficChange = async (row) => {
  try {
    await updateTraffic(row.id, row.traffic)
    ElMessage.success('流量调整成功')
  } catch (e) {
    ElMessage.error('调整失败')
    loadData()
  }
}

const handleComplete = async (row) => {
  try {
    await completeRelease(row.id)
    ElMessage.success('发布已完成')
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleRollback = async (row) => {
  try {
    await rollbackRelease(row.id)
    ElMessage.success('回滚成功')
    loadData()
  } catch (e) {
    ElMessage.error('回滚失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }
</style>
