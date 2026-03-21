<template>
  <div class="monitor-page">
    <div class="page-header">
      <h1>监控中心</h1>
      <div class="header-actions">
        <el-select v-model="refreshInterval" placeholder="刷新间隔" style="width: 120px">
          <el-option label="5秒" :value="5000" />
          <el-option label="10秒" :value="10000" />
          <el-option label="30秒" :value="30000" />
          <el-option label="手动" :value="0" />
        </el-select>
        <el-button :icon="Refresh" @click="loadData">刷新</el-button>
      </div>
    </div>
    
    <!-- 监控概览 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="m in metrics" :key="m.name">
        <el-card class="metric-card">
          <div class="metric-header">
            <span class="metric-name">{{ m.name }}</span>
            <el-tag :type="m.status === 'normal' ? 'success' : m.status === 'warning' ? 'warning' : 'danger'" size="small">
              {{ m.statusText }}
            </el-tag>
          </div>
          <div class="metric-value">
            {{ m.value }}<span class="metric-unit">{{ m.unit }}</span>
          </div>
          <el-progress :percentage="m.percentage" :color="m.color" :status="m.percentage > 90 ? 'exception' : ''" />
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>CPU 使用率趋势</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="1h">1小时</el-radio-button>
                <el-radio-button label="6h">6小时</el-radio-button>
                <el-radio-button label="24h">24小时</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="cpuChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>服务状态</span>
          </template>
          <div class="service-status">
            <div v-for="s in services" :key="s.name" class="service-item">
              <div class="service-info">
                <span class="service-name">{{ s.name }}</span>
                <span class="service-uptime">运行: {{ s.uptime }}</span>
              </div>
              <el-tag :type="s.status === 'running' ? 'success' : 'danger'" size="small">
                {{ s.status === 'running' ? '在线' : '离线' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 告警列表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>告警记录</span>
          <el-badge :value="alertCount" :max="99">
            <el-button size="small">查看全部</el-button>
          </el-badge>
        </div>
      </template>
      <el-table :data="alerts" style="width: 100%">
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="row.level === 'critical' ? 'danger' : row.level === 'warning' ? 'warning' : 'info'">
              {{ row.levelText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="告警信息" min-width="300" />
        <el-table-column prop="source" label="来源" width="120" />
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" link>处理</el-button>
            <el-button size="small" type="primary" link>详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const cpuChartRef = ref(null)
let cpuChart = null

const refreshInterval = ref(10000)
let timer = null

const metrics = ref([
  { name: 'CPU 使用率', value: 45, unit: '%', percentage: 45, color: '#667eea', status: 'normal', statusText: '正常' },
  { name: '内存使用', value: 62, unit: '%', percentage: 62, color: '#10b981', status: 'normal', statusText: '正常' },
  { name: '磁盘使用', value: 78, unit: '%', percentage: 78, color: '#f59e0b', status: 'warning', statusText: '警告' },
  { name: '网络IO', value: 120, unit: 'MB/s', percentage: 40, color: '#3b82f6', status: 'normal', statusText: '正常' }
])

const services = ref([
  { name: 'ops-service', status: 'running', uptime: '2天 5小时' },
  { name: 'gateway', status: 'running', uptime: '2天 5小时' },
  { name: 'user-service', status: 'running', uptime: '1天 12小时' },
  { name: 'workflow-service', status: 'offline', uptime: '-' }
])

const alerts = ref([
  { level: 'warning', levelText: '警告', message: '磁盘使用率超过 75%', source: 'monitor', time: '12:00:00' },
  { level: 'info', levelText: '信息', message: '服务 heartbeat 正常', source: 'system', time: '11:55:00' },
  { level: 'critical', levelText: '严重', message: 'workflow-service 无响应', source: 'health', time: '11:30:00' }
])

const timeRange = ref('1h')
const alertCount = computed(() => alerts.value.length)

// 模拟数据更新
const updateMetrics = () => {
  metrics.value = metrics.value.map(m => {
    if (m.name === 'CPU 使用率') {
      const newValue = Math.floor(Math.random() * 40) + 30
      return { ...m, value: newValue, percentage: newValue, status: newValue > 80 ? 'warning' : 'normal', statusText: newValue > 80 ? '警告' : '正常' }
    }
    if (m.name === '内存使用') {
      const newValue = Math.floor(Math.random() * 20) + 50
      return { ...m, value: newValue, percentage: newValue }
    }
    return m
  })
  updateChart()
}

const updateChart = () => {
  if (cpuChart) {
    const now = new Date()
    const time = now.getHours() + ':' + String(now.getMinutes()).padStart(2, '0')
    const option = cpuChart.getOption()
    option.xAxis[0].data.push(time)
    option.series[0].data.push(metrics.value[0].value)
    if (option.xAxis[0].data.length > 20) {
      option.xAxis[0].data.shift()
      option.series[0].data.shift()
    }
    cpuChart.setOption(option)
  }
}

const initChart = () => {
  nextTick(() => {
    cpuChart = echarts.init(cpuChartRef.value)
    cpuChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { 
        type: 'category', 
        boundaryGap: false,
        data: Array.from({length: 20}, (_, i) => `${i}:00`)
      },
      yAxis: { type: 'value', max: 100 },
      series: [{
        type: 'line',
        smooth: true,
        symbol: 'none',
        areaStyle: { color: 'rgba(102, 126, 234, 0.3)' },
        itemStyle: { color: '#667eea' },
        data: Array.from({length: 20}, () => Math.floor(Math.random() * 40) + 30)
      }]
    })
  })
}

const loadData = () => {
  updateMetrics()
}

const startTimer = () => {
  if (timer) clearInterval(timer)
  if (refreshInterval.value > 0) {
    timer = setInterval(updateMetrics, refreshInterval.value)
  }
}

watch(refreshInterval, () => {
  startTimer()
})

onMounted(() => {
  initChart()
  startTimer()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (cpuChart) cpuChart.dispose()
})
</script>

<style scoped>
.monitor-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }
.header-actions { display: flex; gap: 12px; }

.metric-card { margin-bottom: 20px; }
.metric-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.metric-name { color: #6b7280; font-size: 14px; }
.metric-value { font-size: 32px; font-weight: bold; color: #1f2937; margin: 12px 0; }
.metric-unit { font-size: 14px; color: #9ca3af; margin-left: 4px; }

.service-status { max-height: 300px; overflow-y: auto; }
.service-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.service-item:last-child { border-bottom: none; }
.service-info { display: flex; flex-direction: column; gap: 4px; }
.service-name { font-weight: 500; }
.service-uptime { font-size: 12px; color: #9ca3af; }
</style>
