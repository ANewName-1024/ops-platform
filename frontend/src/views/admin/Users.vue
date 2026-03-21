<template>
  <div class="page-container">
    <div class="page-header">
      <h1>用户管理</h1>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>新建用户
      </el-button>
    </div>
    
    <!-- 搜索筛选 -->
    <el-card>
      <div class="search-bar">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索用户名/邮箱..." 
          prefix-icon="Search" 
          clearable 
          style="width: 300px" 
          @input="handleSearch"
        />
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="loadData">
          <el-option label="全部" value="" />
          <el-option label="启用" value="true" />
          <el-option label="禁用" value="false" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
      
      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32">{{ (row.username || 'U').charAt(0).toUpperCase() }}</el-avatar>
              <span>{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="nickname" label="昵称" width="120">
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.role === 'ADMIN' ? 'danger' : row.role === 'OPS' ? 'warning' : 'info'">
              {{ { ADMIN: '管理员', OPS: '运维', USER: '用户' }[row.role] || row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-switch 
              v-model="row.enabled" 
              @change="handleStatusChange(row)" 
              :disabled="row.username === 'admin'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
              :disabled="row.username === 'admin'"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
    
    <!-- 创建/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑用户' : '新建用户'" 
      width="500px" 
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="运维" value="OPS" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ isEdit ? '保存' : '创建' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUsers, createUser, updateUser, deleteUser as deleteUserApi, updateUserStatus } from '../../api/user'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const userList = ref([])
const formRef = ref(null)

const form = reactive({
  id: null,
  username: '',
  email: '',
  nickname: '',
  role: 'USER',
  password: '',
  enabled: true
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur', min: 6 }]
}

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getUsers()
    let data = res.users || []
    
    // 搜索筛选
    if (searchQuery.value) {
      const q = searchQuery.value.toLowerCase()
      data = data.filter(u => 
        (u.username && u.username.toLowerCase().includes(q)) || 
        (u.email && u.email.toLowerCase().includes(q))
      )
    }
    if (statusFilter.value !== '') {
      data = data.filter(u => String(u.enabled) === statusFilter.value)
    }
    
    userList.value = data
    pagination.total = data.length
  } catch (e) {
    console.error('加载失败:', e)
    ElMessage.error('加载用户列表失败: ' + (e.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const openCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    username: row.username,
    email: row.email,
    nickname: row.nickname || '',
    role: row.role || 'USER',
    password: '',
    enabled: row.enabled
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  if (row.username === 'admin') {
    ElMessage.warning('不能删除管理员账户')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', { type: 'warning' })
    await deleteUserApi(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败: ' + (e.message || '未知错误'))
    }
  }
}

const handleStatusChange = async (row) => {
  try {
    await updateUserStatus(row.id, row.enabled)
    ElMessage.success(row.enabled ? '已启用' : '已禁用')
  } catch (e) {
    // 回滚状态
    row.enabled = !row.enabled
    ElMessage.error('操作失败: ' + (e.message || '未知错误'))
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateUser(form.id, form)
        ElMessage.success('保存成功')
      } else {
        await createUser(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      resetForm()
      loadData()
    } catch (e) {
      ElMessage.error((isEdit.value ? '保存' : '创建') + '失败: ' + (e.message || '未知错误'))
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    username: '',
    email: '',
    nickname: '',
    role: 'USER',
    password: '',
    enabled: true
  })
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

.user-cell { display: flex; align-items: center; gap: 8px; }

.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
