<template>
  <div class="page-container">
    <div class="page-header">
      <h1>自愈中心</h1>
      <el-button type="primary" @click="showCreateDialog = true">添加策略</el-button>
    </div>
    <el-card>
      <el-table :data="strategies" style="width: 100%">
        <el-table-column prop="alertType" label="触发条件" />
        <el-table-column prop="action" label="执行动作" />
        <el-table-column prop="enabled" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '禁用' }}</el-tag>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHealStrategies, deleteHealStrategy } from '../../api/autoheal'

const strategies = ref([])
const showCreateDialog = ref(false)

const loadData = async () => {
  try {
    const res = await getHealStrategies()
    strategies.value = res.strategies || []
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

const editStrategy = (row) => { ElMessage.info('编辑: ' + row.alertType) }
const deleteStrategy = async (row) => {
  try {
    await deleteHealStrategy(row.alertType)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { ElMessage.error('删除失败') }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }
</style>
