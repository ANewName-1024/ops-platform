<template>
  <div class="dashboard">
    <div class="page-header">
      <h1>仪表盘</h1>
      <div class="header-actions">
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <el-icon :size="28"><Operation /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalTasks }}</div>
            <div class="stat-label">总任务数</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #10b981 0%, #059669 100%)">
            <el-icon :size="28"><Loading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.runningTasks }}</div>
            <div class="stat-label">执行中</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)">
            <el-icon :size="28"><SuccessFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completedTasks }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%)">
            <el-icon :size="28"><WarningFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.failedTasks }}</div>
            <div class="stat-label">失败</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>CPU 使用率趋势</span>
          </template>
          <div ref="cpuChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>内存使用率</span>
          </template>
          <div ref="memoryChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 活动与告警 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>最近活动</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in activities"
              :key="index"
              :timestamp="item.time"
              :type="item.type"
              :icon="item.icon"
            >
              {{ item.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>待处理告警</span>
          </template>
          <el-table :data="alerts" style="width: 100%">
            <el-table-column prop="level" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="row.level === 'high' ? 'danger' : 'warning'">
                  {{ row.level === 'high' ? '严重' : '警告' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="告警信息" />
            <el-table-column prop="time" label="时间" width="120" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Refresh, Operation, Loading, SuccessFilled, WarningFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboardOverview } from '../../api/dashboard'

const cpuChartRef = ref(null)
const memoryChartRef = ref(null)

const stats = ref({
  totalTasks: 0,
  runningTasks: 0,
  completedTasks: 0,
  failedTasks: 0
})

const activities = ref([
  { time: '10:30', content: '用户 admin 登录系统', type: 'primary', icon: 'User' },
  { time: '10:25', content: '完成服务部署任务 #1234', type: 'success', icon: 'Check' },
  { time: '10:15', content: '触发自愈策略: CPU > 90%', type: 'warning', icon: 'Warning' },
  { time: '10:00', content: '灰度发布 v2.1.0 完成', type: 'success', icon: 'Check' }
])

const alerts = ref([
  { level: 'high', message: '服务器 CPU 使用率超过 90%', time: '10:30' },
  { level: 'warning', message: '数据库连接数接近上限', time: '10:15' },
  { level: 'warning', message: '证书即将过期 (30天)', time: '09:45' }
])

const loadData = async () => {
  try {
    const res = await getDashboardOverview()
    if (res) {
      stats.value = {
        totalTasks: res.totalTasks || 128,
        runningTasks: res.runningTasks || 3,
        completedTasks: res.completedTasks || 120,
        failedTasks: res.failedTasks || 5
      }
    }
  } catch (e) {
    // 使用默认数据
  }
}

const initCharts = () => {
  // CPU 图表
  const cpuChart = echarts.init(cpuChartRef.value)
  cpuChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'] },
    yAxis: { type: 'value', max: 100 },
    series: [{
      data: [45, 32, 65, 78, 55, 42],
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#667eea' }
    }]
  })
  
  // 内存图表
  const memoryChart = echarts.init(memoryChartRef.value)
  memoryChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['50%', '70%'],
      data: [
        { value: 62, name: '已使用', itemStyle: { color: '#667eea' } },
        { value: 38, name: '空闲', itemStyle: { color: '#e5e7eb' } }
      ],
      label: { show: true, formatter: '{d}%' }
    }]
  })
}

onMounted(async () => {
  await loadData()
  await nextTick()
  initCharts()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  color: #1f2937;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  margin-left: 16px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1f2937;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}
</style>
