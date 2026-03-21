<template>
  <div class="page-container">
    <div class="page-header">
      <h1>灰度发布</h1>
      <el-button type="primary">新建发布</el-button>
    </div>
    <el-card>
      <el-table :data="releases" style="width: 100%">
        <el-table-column prop="version" label="版本" />
        <el-table-column prop="service" label="服务" />
        <el-table-column prop="traffic" label="流量分配">
          <template #default="{ row }">{{ row.traffic }}%</template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'success' : row.status === 'rollingback' ? 'warning' : 'primary'">
              {{ { completed: '已完成', running: '进行中', rollingback: '回滚中' }[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small">详情</el-button>
            <el-button size="small" type="warning" v-if="row.status === 'running'">回滚</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getReleases } from '../../api/autoheal'

const releases = ref([])

const loadData = async () => {
  try {
    const res = await getReleases()
    releases.value = res.releases || []
  } catch (e) { }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }
</style>
