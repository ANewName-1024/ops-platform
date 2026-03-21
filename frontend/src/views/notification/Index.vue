<template>
  <div class="page-container">
    <div class="page-header">
      <h1>通知中心</h1>
      <el-button>全部已读</el-button>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="未读" name="unread" />
      <el-tab-pane label="系统" name="system" />
      <el-tab-pane label="告警" name="alert" />
    </el-tabs>
    <el-card>
      <el-table :data="notifications" style="width: 100%">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <span>{{ { system: '🖥️', alert: '🔔', task: '📋' }[row.type] || '📌' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="time" label="时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('all')
const notifications = ref([
  { type: 'alert', title: '系统告警', content: 'CPU使用率超过90%', time: '10:30' },
  { type: 'system', title: '系统通知', content: '系统将于今晚23:00维护', time: '09:00' },
  { type: 'task', title: '任务完成', content: '部署任务已完成', time: '昨天' }
])
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }
</style>
