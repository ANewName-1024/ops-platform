<template>
  <div class="page-container">
    <div class="page-header">
      <h1>系统配置</h1>
      <el-button type="primary" @click="saveConfig" :loading="saving">
        <el-icon><Select /></el-icon>保存配置
      </el-button>
    </div>
    
    <el-tabs v-model="activeTab">
      <!-- 基本设置 -->
      <el-tab-pane label="基本设置" name="basic">
        <el-card>
          <el-form label-width="120px">
            <el-form-item label="系统名称">
              <el-input v-model="config.system.name" placeholder="请输入系统名称" />
            </el-form-item>
            <el-form-item label="系统描述">
              <el-input v-model="config.system.description" type="textarea" :rows="3" placeholder="请输入系统描述" />
            </el-form-item>
            <el-divider>时区与语言</el-divider>
            <el-form-item label="时区">
              <el-select v-model="config.system.timezone" style="width: 100%">
                <el-option label="中国上海 (Asia/Shanghai)" value="Asia/Shanghai" />
                <el-option label="UTC" value="UTC" />
              </el-select>
            </el-form-item>
            <el-form-item label="语言">
              <el-select v-model="config.system.language" style="width: 100%">
                <el-option label="简体中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 邮件配置 -->
      <el-tab-pane label="邮件配置" name="mail">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>SMTP 邮件配置</span>
              <el-switch v-model="config.mail.enabled" active-text="启用" inactive-text="禁用" />
            </div>
          </template>
          <el-form label-width="120px" :disabled="!config.mail.enabled">
            <el-form-item label="SMTP主机">
              <el-input v-model="config.mail.host" placeholder="smtp.example.com" />
            </el-form-item>
            <el-form-item label="SMTP端口">
              <el-input-number v-model="config.mail.port" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="加密方式">
              <el-radio-group v-model="config.mail.encryption">
                <el-radio label="none">无</el-radio>
                <el-radio label="ssl">SSL</el-radio>
                <el-radio label="tls">TLS</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="config.mail.username" placeholder="noreply@example.com" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="config.mail.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="testMail" :loading="testingMail">
                <el-icon><Message /></el-icon>发送测试邮件
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 安全设置 -->
      <el-tab-pane label="安全设置" name="security">
        <el-card>
          <template #header>
            <span>密码策略</span>
          </template>
          <el-form label-width="180px">
            <el-form-item label="强制密码复杂度">
              <div style="display: flex; align-items: center; gap: 12px;">
                <el-switch v-model="config.security.password.strict" />
                <span class="form-tip">密码必须包含大小写字母，数字和特殊字符</span>
              </div>
            </el-form-item>
            <el-form-item label="最小密码长度">
              <el-input-number v-model="config.security.password.minLength" :min="6" :max="32" />
            </el-form-item>
            <el-form-item label="密码过期天数">
              <div style="display: flex; align-items: center; gap: 12px;">
                <el-switch v-model="config.security.password.expiry.enabled" />
                <el-input-number v-model="config.security.password.expiry.days" :min="30" :max="365" :disabled="!config.security.password.expiry.enabled" />
                <span class="form-tip">天</span>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card style="margin-top: 20px;">
          <template #header>
            <span>登录安全</span>
          </template>
          <el-form label-width="180px">
            <el-form-item label="登录失败锁定">
              <div style="display: flex; align-items: center; gap: 12px;">
                <el-switch v-model="config.security.login.lock.enabled" />
                <span class="form-tip">连续失败5次锁定30分钟</span>
              </div>
            </el-form-item>
            <el-form-item label="最大登录尝试次数">
              <el-input-number v-model="config.security.login.maxAttempts" :min="3" :max="10" :disabled="!config.security.login.lock.enabled" />
            </el-form-item>
            <el-form-item label="开启双因素认证">
              <el-switch v-model="config.security.twofactor.enabled" />
            </el-form-item>
            <el-form-item label="会话超时">
              <el-input-number v-model="config.security.session.timeout" :min="5" :max="480" />
              <span class="form-tip" style="margin-left: 12px;">分钟</span>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      
      <!-- 系统信息 -->
      <el-tab-pane label="系统信息" name="info">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>服务器信息</span>
              </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="主机名">WEI3216</el-descriptions-item>
                <el-descriptions-item label="操作系统">Windows Server 10.0</el-descriptions-item>
                <el-descriptions-item label="系统架构">amd64</el-descriptions-item>
                <el-descriptions-item label="CPU核心数">4</el-descriptions-item>
                <el-descriptions-item label="Java版本">21.0.8</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>内存信息</span>
              </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="总内存">1024 MB</el-descriptions-item>
                <el-descriptions-item label="已使用内存">512 MB</el-descriptions-item>
                <el-descriptions-item label="空闲内存">512 MB</el-descriptions-item>
                <el-descriptions-item label="最大内存">2048 MB</el-descriptions-item>
                <el-descriptions-item label="启动时间">{{ systemInfo.startTime }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>
        
        <el-card style="margin-top: 20px;">
          <template #header>
            <span>运行指标</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="metric-box">
                <div class="metric-label">CPU使用率</div>
                <div class="metric-value">45%</div>
                <el-progress :percentage="45" :color="'#667eea'" />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-box">
                <div class="metric-label">内存使用</div>
                <div class="metric-value">62%</div>
                <el-progress :percentage="62" :color="'#10b981'" />
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-box">
                <div class="metric-label">磁盘使用</div>
                <div class="metric-value">78%</div>
                <el-progress :percentage="78" :color="'#f59e0b'" />
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Select, Message } from '@element-plus/icons-vue'
import { getSystemConfig, updateSystemConfig } from '../../api/system'

const activeTab = ref('basic')
const saving = ref(false)
const testingMail = ref(false)

const config = reactive({
  system: {
    name: 'OPS 平台',
    description: '运维管理一体化平台',
    timezone: 'Asia/Shanghai',
    language: 'zh-CN'
  },
  mail: {
    enabled: false,
    host: 'smtp.example.com',
    port: 587,
    encryption: 'tls',
    username: '',
    password: ''
  },
  security: {
    password: {
      strict: true,
      minLength: 8,
      expiry: {
        enabled: true,
        days: 90
      }
    },
    login: {
      maxAttempts: 5,
      lock: {
        enabled: true,
        minutes: 30
      }
    },
    twofactor: {
      enabled: false
    },
    session: {
      timeout: 30
    }
  }
})

const systemInfo = reactive({
  startTime: new Date().toLocaleString()
})

const loadConfig = async () => {
  try {
    const res = await getSystemConfig()
    if (res.config) {
      Object.assign(config.system, {
        name: res.config['system.name'] || config.system.name,
        description: res.config['system.description'] || config.system.description,
        timezone: res.config['system.timezone'] || config.system.timezone
      })
    }
  } catch (e) {
    console.error('加载配置失败')
  }
}

const saveConfig = async () => {
  saving.value = true
  try {
    await updateSystemConfig(config.system)
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const testMail = () => {
  testingMail.value = true
  setTimeout(() => {
    testingMail.value = false
    ElMessage.success('测试邮件发送成功')
  }, 1500)
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }

.form-tip { color: #9ca3af; font-size: 13px; }

.metric-box { text-align: center; padding: 20px; background: #f9fafb; border-radius: 12px; }
.metric-label { color: #6b7280; font-size: 14px; margin-bottom: 8px; }
.metric-value { font-size: 32px; font-weight: bold; color: #1f2937; margin-bottom: 12px; }
</style>
