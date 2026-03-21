<template>
  <div class="page-container">
    <div class="page-header">
      <h1>自愈中心</h1>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>添加策略
      </el-button>
    </div>
    <el-card>
      <el-table :data="strategies" style="width: 100%" v-loading="loading">
        <el-table-column prop="alertType" label="触发条件" />
        <el-table-column prop="action" label="执行动作" />
        <el-table-column prop="enabled" label="状态">
          <template #default="{ row }">
            <el-switch 
              v-model="row.enabled" 
              @change="handleToggle(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="editStrategy(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteStrategy(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑策略' : '添加策略'" width="500px">
      <el-form :model="strategyForm" label-width="100px">
        <el-form-item label="触发条件">
          <el-input v-model="strategyForm.alertType" placeholder="如: CPU_HIGH" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="执行动作">
          <el-input v-model="strategyForm.action" placeholder="如: 重启服务" />
        </el-form-item>
        <el-form-item label="脚本内容">
          <el-input v-model="strategyForm.script" type="textarea" :rows="4" placeholder="执行脚本内容" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="strategyForm.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getHealStrategies, createHealStrategy, deleteHealStrategy } from '../../api/autoheal'

const strategies = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)

const strategyForm = ref({
  alertType: '',
  action: '',
  script: '',
  enabled: true
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getHealStrategies()
    strategies.value = res.strategies || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  strategyForm.value = {
    alertType: '',
    action: '',
    script: '',
    enabled: true
  }
  dialogVisible.value = true
}

const editStrategy = (row) => {
  isEdit.value = true
  strategyForm.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!strategyForm.value.alertType || !strategyForm.value.action) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  saving.value = true
  try {
    await createHealStrategy(strategyForm.value)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const handleToggle = async (row) => {
  try {
    await createHealStrategy({ ...row })
    ElMessage.success('状态已更新')
  } catch (e) {
    ElMessage.error('更新失败')
    loadData()
  }
}

const deleteStrategy = async (row) => {
  try {
    await deleteHealStrategy(row.alertType)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error('删除失败')
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
