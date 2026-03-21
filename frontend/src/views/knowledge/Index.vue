<template>
  <div class="knowledge-page">
    <div class="page-header">
      <h1>知识库</h1>
      <div class="header-actions">
        <el-input
          v-model="searchQuery"
          placeholder="搜索知识库..."
          prefix-icon="Search"
          style="width: 300px"
          clearable
          @clear="loadData"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>新建知识库
        </el-button>
      </div>
    </div>
    
    <!-- 知识库卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="kb in knowledgeBases" :key="kb.id">
        <el-card class="kb-card" shadow="hover">
          <div class="kb-header">
            <div class="kb-icon">📚</div>
            <el-dropdown trigger="click" @command="handleCommand($event, kb)">
              <el-button :icon="MoreFilled" text circle />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="delete">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <h3 class="kb-title">{{ kb.name }}</h3>
          <p class="kb-description">{{ kb.description || '暂无描述' }}</p>
          <div class="kb-meta">
            <span><el-icon><Document /></el-icon> {{ kb.documentCount || 0 }} 篇</span>
            <span><el-icon><Clock /></el-icon> {{ kb.createTime || '最近' }}</span>
          </div>
          <div class="kb-actions">
            <el-button size="small" type="primary" @click="viewDocuments(kb)">查看文档</el-button>
            <el-button size="small" @click="uploadDocument(kb)">上传</el-button>
          </div>
        </el-card>
      </el-col>
      
      <!-- 新建卡片 -->
      <el-col :span="6">
        <el-card class="kb-card add-card" @click="showCreateDialog = true">
          <el-icon :size="48" color="#9ca3af"><Plus /></el-icon>
          <p>新建知识库</p>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 文档列表抽屉 -->
    <el-drawer v-model="showDocumentDrawer" :title="currentKB?.name" size="60%">
      <div class="drawer-header">
        <el-input
          v-model="docSearchQuery"
          placeholder="搜索文档..."
          prefix-icon="Search"
          clearable
          style="width: 300px"
        />
        <el-button type="primary" @click="uploadDocument(currentKB)">
          <el-icon><Upload /></el-icon>上传文档
        </el-button>
      </div>
      
      <el-table :data="documents" v-loading="docLoading">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type || '文档' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDocument(row)">查看</el-button>
            <el-button size="small" type="danger" @click="deleteDocument(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
    
    <!-- 创建知识库对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建知识库" width="500px">
      <el-form :model="createForm" label-width="80px" ref="createFormRef">
        <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入名称', trigger: 'blur' }]">
          <el-input v-model="createForm.name" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="createForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="技术文档" value="tech" />
            <el-option label="操作手册" value="manual" />
            <el-option label="常见问题" value="faq" />
            <el-option label="API文档" value="api" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MoreFilled, Document, Clock, Upload } from '@element-plus/icons-vue'
import { 
  getKnowledgeBases, 
  createKnowledgeBase, 
  deleteKnowledgeBase as deleteKBApi,
  getDocuments,
  deleteDocument as deleteDocApi
} from '../../api/knowledge'

const searchQuery = ref('')
const showCreateDialog = ref(false)
const creating = ref(false)
const knowledgeBases = ref([])
const createFormRef = ref(null)
const createForm = reactive({
  name: '',
  description: '',
  category: ''
})

// 文档相关
const showDocumentDrawer = ref(false)
const currentKB = ref(null)
const documents = ref([])
const docLoading = ref(false)
const docSearchQuery = ref('')

const loadData = async () => {
  try {
    const res = await getKnowledgeBases()
    knowledgeBases.value = res.bases || []
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

const handleSearch = () => {
  loadData()
}

const handleCommand = (command, kb) => {
  if (command === 'edit') {
    ElMessage.info('编辑功能开发中')
  } else if (command === 'delete') {
    deleteKnowledgeBase(kb)
  }
}

const deleteKnowledgeBase = async (kb) => {
  try {
    await ElMessageBox.confirm(`确定要删除知识库 "${kb.name}" 吗？`, '提示', { type: 'warning' })
    await deleteKBApi(kb.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleCreate = async () => {
  if (!createForm.name) {
    ElMessage.warning('请输入名称')
    return
  }
  creating.value = true
  try {
    await createKnowledgeBase(createForm)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    Object.assign(createForm, { name: '', description: '', category: '' })
    loadData()
  } catch (e) {
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

const viewDocuments = async (kb) => {
  currentKB.value = kb
  showDocumentDrawer.value = true
  docLoading.value = true
  try {
    const res = await getDocuments(kb.id)
    documents.value = res.documents || []
  } catch (e) {
    ElMessage.error('加载文档失败')
  } finally {
    docLoading.value = false
  }
}

const uploadDocument = (kb) => {
  ElMessage.info(`上传文档到: ${kb.name}`)
}

const viewDocument = (doc) => {
  ElMessage.info(`查看文档: ${doc.title}`)
}

const deleteDocument = async (doc) => {
  try {
    await ElMessageBox.confirm(`确定要删除文档 "${doc.title}" 吗？`, '提示', { type: 'warning' })
    await deleteDocApi(doc.id)
    ElMessage.success('删除成功')
    viewDocuments(currentKB.value)
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.knowledge-page { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }
.header-actions { display: flex; gap: 12px; }

.kb-card {
  margin-bottom: 20px;
  transition: all 0.3s;
  cursor: pointer;
}
.kb-card:hover { transform: translateY(-4px); }

.kb-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.kb-icon {
  font-size: 36px;
}

.kb-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.kb-description {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 12px;
  min-height: 40px;
}

.kb-meta {
  display: flex;
  gap: 16px;
  color: #9ca3af;
  font-size: 13px;
  margin-bottom: 16px;
}

.kb-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.kb-actions {
  display: flex;
  gap: 8px;
}

.add-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  border: 2px dashed #e5e7eb;
  background: #fafafa;
}
.add-card:hover {
  border-color: #667eea;
  background: #f5f7ff;
}
.add-card p {
  margin-top: 12px;
  color: #9ca3af;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
