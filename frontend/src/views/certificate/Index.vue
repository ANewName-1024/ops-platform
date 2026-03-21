<template>
  <div class="page-container">
    <div class="page-header">
      <h1>证书管理</h1>
      <el-button type="primary" @click="loadData">
        <el-icon><Refresh /></el-icon>刷新状态
      </el-button>
    </div>
    
    <el-card>
      <div class="search-bar">
        <el-input v-model="searchQuery" placeholder="搜索域名..." prefix-icon="Search" clearable style="width: 300px" @input="handleSearch" />
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="有效" value="valid" />
          <el-option label="过期" value="expired" />
          <el-option label="即将过期" value="expiring" />
        </el-select>
      </div>
      
      <el-table :data="filteredCerts" style="width: 100%" v-loading="loading">
        <el-table-column prop="domain" label="证书域名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="issuer" label="颁发者" min-width="150" />
        <el-table-column prop="issueDate" label="签发日期" width="120" />
        <el-table-column prop="expireDate" label="过期日期" width="120">
          <template #default="{ row }">
            <span :class="{ 'expired': isExpired(row.expireDate), 'expiring': isExpiringSoon(row.expireDate) }">
              {{ row.expireDate }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="daysRemaining" label="剩余天数" width="100">
          <template #default="{ row }">
            <el-tag :type="getDaysType(row.daysRemaining)">{{ row.daysRemaining }}天</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row)">详情</el-button>
            <el-button size="small" type="warning" @click="handleRenew(row)">续期</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 证书详情对话框 -->
    <el-dialog v-model="detailVisible" title="证书详情" width="600px">
      <el-descriptions :column="2" border v-if="currentCert">
        <el-descriptions-item label="域名">{{ currentCert.domain }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentCert.status)">{{ getStatusText(currentCert.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="颁发者">{{ currentCert.issuer }}</el-descriptions-item>
        <el-descriptions-item label="剩余天数">
          <el-tag :type="getDaysType(currentCert.daysRemaining)">{{ currentCert.daysRemaining }}天</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="签发日期">{{ currentCert.issueDate }}</el-descriptions-item>
        <el-descriptions-item label="过期日期">{{ currentCert.expireDate }}</el-descriptions-item>
        <el-descriptions-item label="证书序列号" :span="2">{{ currentCert.serialNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="签名算法" :span="2">{{ currentCert.signatureAlgorithm || 'SHA256withRSA' }}</el-descriptions-item>
        <el-descriptions-item label="公钥算法" :span="2">{{ currentCert.publicKeyAlgorithm || 'RSA 2048' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleRenew(currentCert)">立即续期</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getCertificateInfo } from '../../api/ops'

const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const detailVisible = ref(false)
const currentCert = ref(null)

const certificates = ref([
  { id: 1, domain: 'api.example.com', issuer: "Let's Encrypt", issueDate: '2024-01-15', expireDate: '2025-04-15', daysRemaining: 390, status: 'valid', serialNumber: '04:F5:8C:3D:...', signatureAlgorithm: 'SHA256withRSA', publicKeyAlgorithm: 'RSA 2048' },
  { id: 2, domain: 'web.example.com', issuer: "Let's Encrypt", issueDate: '2024-06-01', expireDate: '2025-06-01', daysRemaining: 67, status: 'expiring', serialNumber: '0A:1B:2C:3D:...', signatureAlgorithm: 'SHA256withRSA', publicKeyAlgorithm: 'RSA 2048' },
  { id: 3, domain: 'admin.example.com', issuer: 'DigiCert', issueDate: '2023-03-01', expireDate: '2024-03-01', daysRemaining: 0, status: 'expired', serialNumber: '1A:2B:3C:4D:...', signatureAlgorithm: 'SHA256withRSA', publicKeyAlgorithm: 'RSA 4096' }
])

const filteredCerts = computed(() => {
  return certificates.value.filter(cert => {
    const matchSearch = !searchQuery.value || cert.domain.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchStatus = !statusFilter.value || cert.status === statusFilter.value
    return matchSearch && matchStatus
  })
})

const isExpired = (date) => {
  return new Date(date) < new Date()
}

const isExpiringSoon = (date) => {
  const diff = new Date(date) - new Date()
  return diff > 0 && diff < 30 * 24 * 60 * 60 * 1000 // 30天内
}

const getDaysType = (days) => {
  if (days <= 0) return 'danger'
  if (days <= 30) return 'warning'
  return 'success'
}

const getStatusType = (status) => {
  const map = { valid: 'success', expiring: 'warning', expired: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { valid: '有效', expiring: '即将过期', expired: '已过期' }
  return map[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCertificateInfo()
    if (res && res.certificates) {
      certificates.value = res.certificates
    }
    ElMessage.success('刷新成功')
  } catch (e) {
    console.error('加载失败:', e)
    ElMessage.error('加载失败: ' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 搜索筛选在 computed 中处理
}

const viewDetail = (row) => {
  currentCert.value = row
  detailVisible.value = true
}

const handleRenew = async (row) => {
  ElMessage.info('开始续期证书: ' + row.domain)
  // 模拟续期
  setTimeout(() => {
    ElMessage.success('证书续期成功')
  }, 1000)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }

.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }

.expired { color: #EF4444; font-weight: 500; }
.expiring { color: #F59E0B; font-weight: 500; }
</style>
