import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'

import './style.css'
import './styles/variables.css'

const app = createApp(App)

// 按需注册 Element Plus 图标（减少初始加载）
import {
  House, Monitor, Operation, Switch, Document, ChatDotRound,
  Lock, Setting, Expand, Fold, Plus, Search, Bell, ArrowDown,
  Refresh, Cpu, MoreFilled, Clock, Upload, ArrowRight
} from '@element-plus/icons-vue'

const icons = {
  House, Monitor, Operation, Switch, Document, ChatDotRound,
  Lock, Setting, Expand, Fold, Plus, Search, Bell, ArrowDown,
  Refresh, Cpu, MoreFilled, Clock, Upload, ArrowRight
}

for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 开启 Vue 性能提示
app.config.performance = true

app.mount('#app')
