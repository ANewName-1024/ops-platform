<template>
  <div class="ai-page">
    <div class="page-header">
      <h1>AI 助手</h1>
    </div>
    <el-card class="chat-container">
      <div class="chat-messages" ref="messagesRef">
        <div v-for="(msg, i) in messages" :key="i" :class="['message', msg.role]">
          <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
          <div class="content">{{ msg.content }}</div>
        </div>
      </div>
      <div class="chat-input">
        <el-input v-model="inputMessage" placeholder="请输入问题..." @keyup.enter="sendMessage" />
        <el-button type="primary" @click="sendMessage" :loading="loading">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { chatAI } from '../../api/ai'

const messages = ref([
  { role: 'assistant', content: '你好！我是 AI 助手，有什么可以帮你的？' }
])
const inputMessage = ref('')
const loading = ref(false)
const messagesRef = ref(null)

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const userMsg = inputMessage.value
  messages.value.push({ role: 'user', content: userMsg })
  inputMessage.value = ''
  loading.value = true
  
  try {
    const res = await chatAI(userMsg)
    messages.value.push({ role: 'assistant', content: res.reply || res.message || '收到回复' })
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.ai-page { padding: 0; }
.page-header { margin-bottom: 24px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }
.chat-container { height: calc(100vh - 200px); display: flex; flex-direction: column; }
.chat-messages { flex: 1; overflow-y: auto; padding: 20px; }
.message { display: flex; margin-bottom: 20px; }
.message.user { flex-direction: row-reverse; }
.avatar { width: 40px; height: 40px; border-radius: 50%; background: #f3f4f6; display: flex; align-items: center; justify-content: center; margin: 0 12px; }
.content { max-width: 70%; padding: 12px 16px; border-radius: 12px; background: #f3f4f6; }
.message.user .content { background: #667eea; color: white; }
.chat-input { display: flex; gap: 12px; padding: 16px; border-top: 1px solid #eee; }
</style>
