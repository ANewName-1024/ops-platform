<template>
  <div class="page-container">
    <div class="page-header">
      <h1>角色管理</h1>
      <el-button type="primary">新建角色</el-button>
    </div>
    <el-card>
      <el-table :data="roles" style="width: 100%">
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="userCount" label="用户数" width="100" />
        <el-table-column label="操作" width="150">
          <template #default>
            <el-button size="small">编辑</el-button>
            <el-button size="small" type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getRoles } from '../../api/user'

const roles = ref([
  { code: 'ADMIN', name: '管理员', description: '超级管理员', userCount: 2 },
  { code: 'OPS', name: '运维', description: '运维人员', userCount: 5 },
  { code: 'USER', name: '普通用户', description: '普通用户', userCount: 10 }
])
</script>

<style scoped>
.page-container { padding: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h1 { margin: 0; font-size: 24px; color: #1f2937; }
</style>
