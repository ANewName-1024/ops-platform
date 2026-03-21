<template>
  <div class="page-container">
    <div class="page-header">
      <h1>运维中心</h1>
      <el-button type="primary" @click="refreshTasks">
        <el-icon><Refresh /></el-icon>刷新
      </el-button>
    </div>
    
    <el-tabs v-model="activeTab">
      <!-- 任务中心 -->
      <el-tab-pane label="任务中心" name="tasks">
        <el-card>
          <div class="tab-header">
            <el-input v-model="taskSearch" placeholder="搜索任务..." prefix-icon="Search" style="width: 300px" />
            <el-select v-model="taskStatus" placeholder="任务状态" clearable style="width: 150px">
              <el-option label="全部" value="" />
              <el-option label="执行中" value="running" />
              <el-option label="已完成" value="completed" />
              <el-option label="失败" value="failed" />
            </el-select>
          </div>
          
          <el-table :data="filteredTasks" style="width: 100%" v-loading="taskLoading">
            <el-table-column prop="name" label="任务名称" min-width="150">
              <template #default="{ row }">
                <div class="task-name">
                  <el-icon><Operation /></el-icon>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="任务类型" width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ getTaskTypeText(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="progress" label="进度" width="200">
              <template #default="{ row }">
                <el-progress :percentage="row.progress" :status="row.status === 'failed' ? 'exception' : row.status === 'completed' ? 'success' : ''" />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="viewTaskDetail(row)">详情</el-button>
                <el-button size="small" type="primary" v-if="row.status === 'running'" @click="stopTask(row)">停止</el-button>
                <el-button size="small" type="warning" v-if="row.status === 'failed'" @click="retryTask(row)">重试</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <!-- 脚本执行 -->
      <el-tab-pane label="脚本执行" name="scripts">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card>
              <template #header>
                <span>脚本配置</span>
              </template>
              <el-form label-width="100px">
                <el-form-item label="选择脚本">
                  <el-select v-model="scriptForm.script" placeholder="请选择脚本" style="width: 100%">
                    <el-option-group label="部署脚本">
                      <el-option label="部署前端" value="deploy_frontend" />
                      <el-option label="部署后端" value="deploy_backend" />
                    </el-option-group>
                    <el-option-group label="运维脚本">
                      <el-option label="重启服务" value="restart_service" />
                      <el-option label="清理日志" value="clean_logs" />
                    </el-option-group>
                    <el-option-group label="备份脚本">
                      <el-option label="备份数据库" value="backup_db" />
                      <el-option label="备份配置" value="backup_config" />
                    </el-option-group>
                  </el-select>
                </el-form-item>
                <el-form-item label="执行参数">
                  <el-input v-model="scriptForm.params" type="textarea" :rows="3" placeholder="请输入执行参数，JSON格式" />
                </el-form-item>
                <el-form-item label="目标主机">
                  <el-select v-model="scriptForm.hosts" multiple placeholder="选择目标主机" style="width: 100%">
                    <el-option label="全部主机" value="all" />
                    <el-option label="192.168.2.32" value="192.168.2.32" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="executeScript" :loading="scriptLoading">
                    <el-icon><Cpu /></el-icon>执行脚本
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card>
              <template #header>
                <span>脚本历史</span>
              </template>
              <el-scrollbar height="400px">
                <div v-for="item in scriptHistory" :key="item.id" class="script-history-item">
                  <div class="history-header">
                    <span class="history-name">{{ item.script }}</span>
                    <el-tag :type="item.status === 'success' ? 'success' : item.status === 'running' ? 'primary' : 'danger'" size="small">
                      {{ item.status }}
                    </el-tag>
                  </div>
                  <div class="history-time">{{ item.time }}</div>
                </div>
              </el-scrollbar>
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 执行日志 -->
        <el-card style="margin-top: 20px" v-if="scriptLog">
          <template #header>
            <span>执行日志</span>
          </template>
          <pre class="script-log">{{ scriptLog }}</pre>
        </el-card>
      </el-tab-pane>
      
      <!-- 日志查看 -->
      <el-tab-pane label="日志查看" name="logs">
        <el-card>
          <div class="tab-header">
            <el-select v-model="logQuery.service" placeholder="选择服务" style="width: 180px">
              <el-option label="全部服务" value="" />
              <el-option label="ops-service" value="ops-service" />
              <el-option label="gateway" value="gateway" />
              <el-option label="user-service" value="user-service" />
              <el-option label="workflow-service" value="workflow-service" />
              <el-option label="knowledge-service" value="knowledge-service" />
            </el-select>
            <el-select v-model="logQuery.level" placeholder="日志级别" style="width: 150px">
              <el-option label="全部" value="" />
              <el-option label="ERROR" value="ERROR" />
              <el-option label="WARN" value="WARN" />
              <el-option label="INFO" value="INFO" />
              <el-option label="DEBUG" value="DEBUG" />
            </el-select>
            <el-select v-model="logQuery.source" placeholder="日志来源" style="width: 180px">
              <el-option label="全部来源" value="" />
              <el-option label="Spring Framework" value="org.springframework" />
              <el-option label="Hibernate" value="org.hibernate" />
              <el-option label="Tomcat" value="org.apache.catalina" />
              <el-option label="业务代码" value="com.example" />
            </el-select>
            <el-button type="primary" @click="queryLogs">
              <el-icon><Search /></el-icon>查询
            </el-button>
            <el-button @click="clearLogs">清空</el-button>
          </div>
          
          <div class="log-toolbar">
            <el-checkbox v-model="logQuery.autoRefresh">自动刷新</el-checkbox>
            <el-select v-model="logQuery.refreshInterval" style="width: 120px" :disabled="!logQuery.autoRefresh">
              <el-option label="3秒" :value="3000" />
              <el-option label="5秒" :value="5000" />
              <el-option label="10秒" :value="10000" />
            </el-select>
            <el-button type="text" @click="exportLogs">导出日志</el-button>
            <el-button type="text" @click="downloadRealLogs">获取实时日志</el-button>
          </div>
          
          <el-scrollbar height="500px">
            <pre class="log-content">{{ logContent }}</pre>
          </el-scrollbar>
        </el-card>
      </el-tab-pane>
      
      <!-- 环境配置 -->
      <el-tab-pane label="环境配置" name="env">
        <el-card>
          <div class="tab-header">
            <el-input v-model="envSearch" placeholder="搜索配置..." prefix-icon="Search" style="width: 300px" />
          </div>
          <el-table :data="filteredEnv" style="width: 100%">
            <el-table-column prop="key" label="配置项" min-width="300" show-overflow-tooltip />
            <el-table-column prop="value" label="值" min-width="400" show-overflow-tooltip>
              <template #default="{ row }">
                <span :class="{ 'secret-value': isSecret(row.key) }">{{ isSecret(row.key) ? '******' : row.value }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 任务详情对话框 -->
    <el-dialog v-model="showTaskDetail" title="任务详情" width="600px">
      <el-descriptions :column="2" border v-if="currentTask">
        <el-descriptions-item label="任务名称">{{ currentTask.name }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ getTaskTypeText(currentTask.type) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTask.status)">{{ getStatusText(currentTask.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="进度">{{ currentTask.progress }}%</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{ currentTask.endTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="执行结果" :span="2">{{ currentTask.result || '无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showTaskDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Operation, Cpu, Search } from '@element-plus/icons-vue'
import { getTasks, executeScript as executeScriptApi } from '../../api/ops'
import { getEnv, getLogs } from '../../api/dashboard'

const activeTab = ref('tasks')
const taskLoading = ref(false)
const taskSearch = ref('')
const taskStatus = ref('')

// 任务数据
const tasks = ref([
  { id: 1, name: '部署前端应用', type: 'deploy', status: 'completed', progress: 100, createTime: '2026-03-21 10:30:00', endTime: '2026-03-21 10:35:00', result: '部署成功' },
  { id: 2, name: '备份数据库', type: 'backup', status: 'running', progress: 45, createTime: '2026-03-21 11:00:00' },
  { id: 3, name: '重启Nginx', type: 'restart', status: 'failed', progress: 30, createTime: '2026-03-21 11:15:00', endTime: '2026-03-21 11:16:00', result: '连接超时' },
  { id: 4, name: '清理日志', type: 'clean', status: 'completed', progress: 100, createTime: '2026-03-21 09:00:00', endTime: '2026-03-21 09:05:00', result: '清理完成，共删除1.2GB' }
])

const filteredTasks = computed(() => {
  return tasks.value.filter(t => {
    const matchSearch = !taskSearch.value || t.name.includes(taskSearch.value)
    const matchStatus = !taskStatus.value || t.status === taskStatus.value
    return matchSearch && matchStatus
  })
})

const getTaskTypeText = (type) => {
  const map = { deploy: '部署', backup: '备份', restart: '重启', clean: '清理' }
  return map[type] || type
}

const getStatusType = (status) => {
  const map = { running: 'primary', completed: 'success', failed: 'danger', pending: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { running: '执行中', completed: '已完成', failed: '失败', pending: '待执行' }
  return map[status] || status
}

const refreshTasks = () => {
  taskLoading.value = true
  setTimeout(() => {
    taskLoading.value = false
    ElMessage.success('刷新成功')
  }, 500)
}

const viewTaskDetail = (task) => {
  currentTask.value = task
  showTaskDetail.value = true
}

const stopTask = (task) => {
  ElMessage.info('停止任务: ' + task.name)
}

const retryTask = (task) => {
  task.status = 'running'
  task.progress = 0
  ElMessage.success('任务已重试')
}

const currentTask = ref(null)
const showTaskDetail = ref(false)

// 脚本执行
const scriptLoading = ref(false)
const scriptForm = reactive({
  script: '',
  params: '',
  hosts: []
})
const scriptHistory = ref([
  { id: 1, script: 'backup_db', status: 'success', time: '10:30:00' },
  { id: 2, script: 'deploy_frontend', status: 'success', time: '11:00:00' },
  { id: 3, script: 'restart_nginx', status: 'failed', time: '11:15:00' }
])
const scriptLog = ref('')

const executeScript = async () => {
  if (!scriptForm.script) {
    ElMessage.warning('请选择脚本')
    return
  }
  scriptLoading.value = true
  try {
    const res = await executeScriptApi(scriptForm)
    scriptLog.value = res.log || '脚本执行完成'
    scriptHistory.value.unshift({
      id: Date.now(),
      script: scriptForm.script,
      status: 'success',
      time: new Date().toLocaleTimeString()
    })
    ElMessage.success('执行成功')
  } catch (e) {
    scriptLog.value = '执行失败: ' + e.message
    ElMessage.error('执行失败')
  } finally {
    scriptLoading.value = false
  }
}

// 日志
const logQuery = reactive({ 
  service: '', 
  level: '', 
  source: '',
  autoRefresh: false,
  refreshInterval: 5000
})
let logTimer = null

const logContent = ref(`========================================
  OPS 平台日志系统
  服务: ops-service
  时间: ${new Date().toLocaleString()}
========================================

[2026-03-21 13:00:00.001] INFO  [main] com.example.ops.OpsServiceApplication - Starting OpsServiceApplication using Java 21.0.8
[2026-03-21 13:00:00.125] INFO  [main] com.example.ops.OpsServiceApplication - No active profile set, falling back to 1 default profile: "default"
[2026-03-21 13:00:00.456] INFO  [main] o.s.d.r.c.RepositoryConfigurationDelegate - Bootstrapping Spring Data JPA in DEFAULT mode
[2026-03-21 13:00:00.512] INFO  [main] o.s.d.r.c.RepositoryConfigurationDelegate - Finished Spring Data repository scanning in 8ms
[2026-03-21 13:00:00.678] INFO  [main] o.s.b.w.e.tomcat.TomcatWebServer - Tomcat initialized with port 8083 (http)
[2026-03-21 13:00:00.890] INFO  [main] o.a.catalina.core.StandardService - Starting Servlet engine: [Apache Tomcat/10.1.33]
[2026-03-21 13:00:01.234] INFO  [main] com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Starting...
[2026-03-21 13:00:01.567] INFO  [main] com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Start completed.
[2026-03-21 13:00:01.678] WARN  [main] org.hibernate.dialect.Dialect - HHH000511: The 11.15.0 version for PostgreSQLDialect is no longer supported
[2026-03-21 13:00:01.890] INFO  [main] o.s.b.w.s.c.ServletWebServerApplicationContext - Root WebApplicationContext: initialization completed in 1021ms
[2026-03-21 13:00:02.123] INFO  [main] o.s.s.c.a.a.c.InitializeUserDetailsBeanManagerConfigurer - Global AuthenticationManager configured
[2026-03-21 13:00:02.456] INFO  [main] o.s.b.a.e.web.EndpointLinksResolver - Exposing 17 endpoints beneath base path '/actuator'
[2026-03-21 13:00:02.567] INFO  [main] o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port 8083 with context path '/'
[2026-03-21 13:00:02.678] INFO  [main] com.example.ops.OpsServiceApplication - Started OpsServiceApplication in 2.678 seconds

[2026-03-21 13:05:00.001] INFO  [http-nio-0.0.0.0-8083-exec-1] com.example.ops.controller.AuthController - User login attempt: admin
[2026-03-21 13:05:00.234] INFO  [http-nio-0.0.0.0-8083-exec-1] com.example.ops.service.AuthService - Login successful for user: admin
[2026-03-21 13:05:00.456] INFO  [http-nio-0.0.0.0-8083-exec-1] o.s.s.w.FilterChainProxy - /ops/auth/login completed in 234ms - 200 OK

[2026-03-21 13:10:00.001] INFO  [http-nio-0.0.0.0-8083-exec-2] com.example.ops.controller.UserController - Getting all users
[2026-03-21 13:10:00.123] INFO  [http-nio-0.0.0.0-8083-exec-2] com.example.ops.service.UserService - Found 2 users
[2026-03-21 13:10:00.234] INFO  [http-nio-0.0.0.0-8083-exec-2] o.s.s.w.FilterChainProxy - /ops/users completed in 234ms - 200 OK

[2026-03-21 13:15:00.001] WARN  [http-nio-0.0.0.0-8083-exec-3] com.zaxxer.hikari.pool.PoolBase - HikariPool-1 - Failed to validate connection
[2026-03-21 13:15:00.234] WARN  [http-nio-0.0.0.0-8083-exec-3] com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Connection leak detection triggered

[2026-03-21 13:20:00.001] ERROR [http-nio-0.0.0.0-8083-exec-4] com.example.ops.service.TaskService - Task execution failed: Database connection timeout
[2026-03-21 13:20:00.234] ERROR [http-nio-0.0.0.0-8083-exec-4] o.s.w.s.ExceptionHandlerExceptionResolver - Resolved [org.springframework.dao.DataAccessResourceFailureException]
`)

const queryLogs = () => {
  // 模拟日志查询
  const levels = logQuery.level ? [logQuery.level] : ['ERROR', 'WARN', 'INFO', 'DEBUG']
  const sources = logQuery.source ? [logQuery.source] : ['ops-service', 'gateway', 'Spring Framework', 'Hibernate', 'Tomcat']
  
  let logs = [`========================================\n  日志查询结果\n  筛选条件:\n    - 服务: ${logQuery.service || '全部'}\n    - 级别: ${logQuery.level || '全部'}\n    - 来源: ${logQuery.source || '全部'}\n  时间: ${new Date().toLocaleString()}\n========================================\n`]
  
  for (let i = 0; i < 30; i++) {
    const level = levels[Math.floor(Math.random() * levels.length)]
    const source = sources[Math.floor(Math.random() * sources.length)]
    const time = new Date(Date.now() - i * 60000).toLocaleString()
    const messages = {
      ERROR: ['Connection timeout', 'NullPointerException: Cannot read property of null', 'Database error: deadlock detected', 'OutOfMemoryError: Java heap space', 'SQLException: No operations allowed after connection closed'],
      WARN: ['Memory usage high: 85%', 'Connection pool near limit: 80%', 'Deprecated API used: getApplicationName()', 'Response time slow: 2500ms', 'Cache miss rate high: 45%'],
      INFO: ['Request processed successfully', 'Task completed: backup_db', 'User admin logged in', 'Configuration reloaded', 'Health check passed'],
      DEBUG: ['SQL executed: SELECT * FROM users', 'Cache hit for key: user:1', 'Request parameters: {page:1,size:10}', 'Transaction committed', 'Bean created: UserService']
    }
    const msg = messages[level][Math.floor(Math.random() * messages[level].length)]
    logs.push(`[${time}] ${level.padEnd(5)} [${source.padEnd(30)}] ${msg}`)
  }
  
  logContent.value = logs.join('\n')
  ElMessage.success('日志查询成功')
}

const clearLogs = () => {
  logContent.value = ''
  ElMessage.success('日志已清空')
}

const exportLogs = () => {
  const blob = new Blob([logContent.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `ops-logs-${new Date().toISOString().slice(0,10)}.log`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('日志导出成功')
}

const downloadRealLogs = async () => {
  try {
    const res = await getLogs()
    if (res && res.logs) {
      logContent.value = res.logs.join('\n')
      ElMessage.success('实时日志获取成功')
    }
  } catch (e) {
    ElMessage.error('获取实时日志失败: ' + (e.message || '未知错误'))
  }
}

// 环境配置
const envSearch = ref('')
const envData = ref([
  { key: 'spring.datasource.url', value: 'jdbc:postgresql://localhost:8432/ops' },
  { key: 'spring.datasource.username', value: 'postgres' },
  { key: 'jwt.secret', value: 'mySecretKey123' },
  { key: 'server.port', value: '8083' },
  { key: 'spring.application.name', value: 'ops-service' }
])

const filteredEnv = computed(() => {
  if (!envSearch.value) return envData.value
  const q = envSearch.value.toLowerCase()
  return envData.value.filter(e => e.key.toLowerCase().includes(q) || e.value.toLowerCase().includes(q))
})

const isSecret = (key) => {
  const secretKeys = ['password', 'secret', 'token', 'key', 'credential']
  return secretKeys.some(k => key.toLowerCase().includes(k))
}

// 自动刷新
watch(() => logQuery.autoRefresh, (val) => {
  if (val) {
    logTimer = setInterval(queryLogs, logQuery.refreshInterval)
  } else {
    if (logTimer) clearInterval(logTimer)
  }
})

watch(() => logQuery.refreshInterval, () => {
  if (logQuery.autoRefresh) {
    if (logTimer) clearInterval(logTimer)
    logTimer = setInterval(queryLogs, logQuery.refreshInterval)
  }
})

onMounted(() => {
  loadEnvData()
})

onUnmounted(() => {
  if (logTimer) clearInterval(logTimer)
})

const loadEnvData = async () => {
  try {
    const res = await getEnv()
    if (res.propertySources) {
      const props = []
      res.propertySources.forEach(ps => {
        if (ps.properties) {
          Object.entries(ps.properties).forEach(([key, val]) => {
            props.push({ key, value: val.value })
          })
        }
      })
      envData.value = props.slice(0, 100)
    }
  } catch (e) {
    console.error('加载环境变量失败')
  }
}
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }

.tab-header { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }

.task-name { display: flex; align-items: center; gap: 8px; font-weight: 500; }

.script-history-item { padding: 12px 0; border-bottom: 1px solid #eee; }
.script-history-item:last-child { border-bottom: none; }
.history-header { display: flex; justify-content: space-between; align-items: center; }
.history-name { font-weight: 500; }
.history-time { color: #999; font-size: 12px; margin-top: 4px; }

.script-log { 
  background: #1e1e1e; 
  color: #d4d4d4; 
  padding: 16px; 
  border-radius: 8px; 
  max-height: 300px; 
  overflow: auto;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.log-content {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  font-family: 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  max-height: 500px;
  overflow: auto;
}

.log-toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }

.secret-value { color: #f59e0b; }

.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
