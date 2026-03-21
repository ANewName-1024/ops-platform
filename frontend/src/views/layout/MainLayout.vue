<template>
  <div class="app-container">
    <!-- 顶部导航 -->
    <header class="app-header">
      <div class="header-left">
        <el-button class="menu-toggle" :icon="sidebarCollapsed ? Expand : Fold" @click="toggleSidebar" />
        <div class="logo">
          <span class="logo-icon">🚀</span>
          <span class="logo-text" v-show="!sidebarCollapsed">OPS 平台</span>
        </div>
        <!-- 面包屑 -->
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentRoute.name">{{ currentRoute.name }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <div class="header-center">
        <el-input
          v-model="searchQuery"
          placeholder="搜索功能..."
          prefix-icon="Search"
          class="header-search"
          @keyup.enter="handleSearch"
        />
      </div>
      
      <div class="header-right">
        <el-badge :value="notificationCount" class="notification-badge" :hidden="notificationCount === 0">
          <el-button :icon="Bell" circle @click="showNotifications" />
        </el-badge>
        
        <el-dropdown @command="handleCommand">
          <div class="user-info">
            <el-avatar :size="32">{{ userStore.username?.charAt(0).toUpperCase() || 'U' }}</el-avatar>
            <span class="username">{{ userStore.username || '用户' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="settings">系统设置</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    
    <div class="app-main">
      <!-- 侧边栏遮罩 (移动端) -->
      <div class="sidebar-overlay" :class="{ visible: sidebarVisible && isMobile }" @click="closeSidebar" />
      
      <!-- 侧边栏 -->
      <aside class="app-sidebar" :class="{ collapsed: sidebarCollapsed, 'mobile-open': sidebarVisible && isMobile }">
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :collapse-transition="false"
          router
          class="sidebar-menu"
          background-color="#1f2937"
          text-color="#fff"
          active-text-color="#667eea"
          @select="onMenuSelect"
        >
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          
          <el-sub-menu index="ops">
            <template #title>
              <el-icon><Monitor /></el-icon>
              <span>运维中心</span>
            </template>
            <el-menu-item index="/ops">任务中心</el-menu-item>
            <el-menu-item index="/monitor">监控中心</el-menu-item>
            <el-menu-item index="/autoheal">自愈中心</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/workflow">
            <el-icon><Operation /></el-icon>
            <template #title>工作流</template>
          </el-menu-item>
          
          <el-menu-item index="/grayrelease">
            <el-icon><Switch /></el-icon>
            <template #title>灰度发布</template>
          </el-menu-item>
          
          <el-menu-item index="/knowledge">
            <el-icon><Document /></el-icon>
            <template #title>知识库</template>
          </el-menu-item>
          
          <el-menu-item index="/ai">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>AI 助手</template>
          </el-menu-item>
          
          <el-menu-item index="/notification">
            <el-icon><Bell /></el-icon>
            <template #title>通知中心</template>
          </el-menu-item>
          
          <el-menu-item index="/certificate">
            <el-icon><Lock /></el-icon>
            <template #title>证书管理</template>
          </el-menu-item>
          
          <el-sub-menu index="admin">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/admin/roles">角色管理</el-menu-item>
            <el-menu-item index="/admin/system">系统配置</el-menu-item>
          </el-sub-menu>
        </el-menu>
        
        <div class="sidebar-footer">
          <el-button :icon="sidebarCollapsed ? Expand : Fold" @click="toggleSidebar" />
        </div>
      </aside>
      
      <!-- 主内容区 -->
      <main class="app-content">
        <router-view v-slot="{ Component }">
          <keep-alive :exclude="['AI', 'Notification']">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </keep-alive>
        </router-view>
      </main>
    </div>
    
    <!-- 移动端底部导航 -->
    <nav class="mobile-nav" v-if="isMobile">
      <div class="mobile-nav-item" :class="{ active: activeMenu === '/dashboard' }" @click="router.push('/dashboard')">
        <el-icon><House /></el-icon>
        <span>首页</span>
      </div>
      <div class="mobile-nav-item" :class="{ active: activeMenu === '/ops' || activeMenu === '/monitor' }" @click="router.push('/ops')">
        <el-icon><Monitor /></el-icon>
        <span>运维</span>
      </div>
      <div class="mobile-nav-item" :class="{ active: activeMenu === '/workflow' }" @click="router.push('/workflow')">
        <el-icon><Operation /></el-icon>
        <span>工作流</span>
      </div>
      <div class="mobile-nav-item" :class="{ active: activeMenu === '/notification' }" @click="router.push('/notification')">
        <el-badge :value="notificationCount" :hidden="notificationCount === 0" :max="9">
          <el-icon><Bell /></el-icon>
        </el-badge>
        <span>消息</span>
      </div>
      <div class="mobile-nav-item" :class="{ active: activeMenu.startsWith('/admin') }" @click="router.push('/admin/users')">
        <el-icon><Setting /></el-icon>
        <span>设置</span>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'
import { 
  Bell, ArrowDown, House, Monitor, Operation, Switch, Document, 
  ChatDotRound, Lock, Setting, Expand, Fold 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const searchQuery = ref('')
const sidebarCollapsed = ref(false)
const sidebarVisible = ref(false)
const isMobile = ref(false)
const notificationCount = ref(3)

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

const toggleSidebar = () => {
  if (isMobile.value) {
    sidebarVisible.value = !sidebarVisible.value
  } else {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
}

const closeSidebar = () => {
  sidebarVisible.value = false
}

const onMenuSelect = () => {
  if (isMobile.value) {
    closeSidebar()
  }
}

const handleSearch = () => {
  ElMessage.info('搜索: ' + searchQuery.value)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'settings') {
    router.push('/admin/system')
  } else if (command === 'profile') {
    router.push('/admin/users')
  }
}

const showNotifications = () => {
  router.push('/notification')
}

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) {
    sidebarVisible.value = false
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
}

/* 顶部导航 */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-toggle {
  background: transparent;
  border: none;
  color: white;
  font-size: 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
}

.logo-icon {
  font-size: 24px;
}

.breadcrumb {
  margin-left: 16px;
}

.breadcrumb :deep(.el-breadcrumb__item) {
  color: rgba(255,255,255,0.8) !important;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: rgba(255,255,255,0.9) !important;
}

.header-center {
  flex: 1;
  max-width: 400px;
  margin: 0 24px;
}

.header-search :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  box-shadow: none;
}

.header-search :deep(.el-input__inner) {
  color: white;
}

.header-search :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.7);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-badge :deep(.el-button) {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.2);
  transition: background 0.3s;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.3);
}

.username {
  font-size: 14px;
}

/* 主布局 */
.app-main {
  display: flex;
  flex: 1;
  overflow: hidden;
  margin-top: 64px;
}

/* 侧边栏遮罩 */
.sidebar-overlay {
  display: none;
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 40;
  opacity: 0;
  transition: opacity 0.3s;
}

.sidebar-overlay.visible {
  display: block;
  opacity: 1;
}

/* 侧边栏 */
.app-sidebar {
  width: 220px;
  background: #1f2937;
  display: flex;
  flex-direction: column;
  transition: width 0.3s, transform 0.3s;
  position: fixed;
  top: 64px;
  left: 0;
  bottom: 0;
  z-index: 50;
}

.app-sidebar.collapsed {
  width: 64px;
}

/* 移动端侧边栏 */
.app-sidebar.mobile-open {
  transform: translateX(0);
}

.sidebar-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
}

.sidebar-menu :deep(.el-sub-menu .el-menu-item) {
  height: 44px;
  line-height: 44px;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255,255,255,0.1);
  display: flex;
  justify-content: center;
}

.sidebar-footer .el-button {
  background: transparent;
  border: none;
  color: rgba(255,255,255,0.6);
}

.sidebar-footer .el-button:hover {
  color: white;
}

/* 主内容区 */
.app-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #f5f7fa;
  margin-left: 220px;
  transition: margin-left 0.3s;
}

.app-sidebar.collapsed + .app-content {
  margin-left: 64px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 移动端底部导航 */
.mobile-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: white;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  z-index: 100;
  justify-content: space-around;
  align-items: center;
}

.mobile-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #999;
  font-size: 12px;
  cursor: pointer;
}

.mobile-nav-item .el-icon {
  font-size: 20px;
}

.mobile-nav-item.active {
  color: #667eea;
}

/* ========== 响应式样式 ========== */

/* 平板 (768px - 1024px) */
@media (max-width: 1024px) {
  .header-center {
    max-width: 300px;
  }
  
  .breadcrumb {
    display: none;
  }
}

/* 手机 (< 768px) */
@media (max-width: 768px) {
  .header-center {
    display: none;
  }
  
  .breadcrumb {
    display: none;
  }
  
  .username {
    display: none;
  }
  
  .logo-text {
    display: none;
  }
  
  .app-sidebar {
    transform: translateX(-100%);
    width: 220px;
  }
  
  .app-sidebar.collapsed {
    width: 220px;
  }
  
  .app-content {
    margin-left: 0;
    padding: 16px;
    padding-bottom: 80px;
  }
  
  .mobile-nav {
    display: flex;
  }
  
  .sidebar-footer {
    display: none;
  }
  
  .menu-toggle {
    display: flex;
  }
}

/* 大屏 (> 1440px) */
@media (min-width: 1440px) {
  .app-content {
    max-width: 1600px;
    margin-left: auto;
    margin-right: auto;
  }
}
</style>
