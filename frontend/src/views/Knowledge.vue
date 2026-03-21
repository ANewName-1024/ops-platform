<template>
  <div class="knowledge">
    <div class="header">
      <h1>知识库</h1>
      <el-button type="primary" @click="showCreateDialog = true">新建知识库</el-button>
    </div>
    
    <el-table :data="knowledgeBases" v-loading="loading">
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="documentCount" label="文档数" width="100" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="viewDocuments(row)">文档</el-button>
          <el-button size="small" type="danger" @click="deleteKnowledgeBase(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 新建知识库对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建知识库" width="500px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="createForm.name" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createKnowledgeBase" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const knowledgeBases = ref([])
const showCreateDialog = ref(false)
const creating = ref(false)

const createForm = ref({
  name: '',
  description: ''
})

const fetchKnowledgeBases = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/ops/knowledge/bases',
      method: 'get'
    })
    knowledgeBases.value = res.bases || []
  } catch (error) {
    ElMessage.error(error.message || '获取知识库列表失败')
  } finally {
    loading.value = false
  }
}

const createKnowledgeBase = async () => {
  if (!createForm.value.name) {
    ElMessage.warning('请输入知识库名称')
    return
  }
  
  creating.value = true
  try {
    await request({
      url: '/ops/knowledge/bases',
      method: 'post',
      data: createForm.value
    })
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    createForm.value = { name: '', description: '' }
    fetchKnowledgeBases()
  } catch (error) {
    ElMessage.error(error.message || '创建失败')
  } finally {
    creating.value = false
  }
}

const viewDocuments = (row) => {
  ElMessage.info(`查看知识库: ${row.name}`)
}

const deleteKnowledgeBase = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除知识库 "${row.name}" 吗？`, '提示', {
      type: 'warning'
    })
    
    await request({
      url: `/ops/knowledge/bases/${row.id}`,
      method: 'delete'
    })
    ElMessage.success('删除成功')
    fetchKnowledgeBases()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  fetchKnowledgeBases()
})
</script>

<style scoped>
.knowledge {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
}
</style>
